package com.skul9x.locateshare

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skul9x.locateshare.adapter.FavoriteCardAdapter
import com.skul9x.locateshare.network.ApiService
import com.skul9x.locateshare.network.FavoriteLocation
import com.skul9x.locateshare.network.RetrofitClient
import com.skul9x.locateshare.util.DoubleTapHandler
import com.skul9x.locateshare.util.INetworkConnectivityObserver
import com.skul9x.locateshare.util.NetworkConnectivityObserver
import com.skul9x.locateshare.util.NetworkUtils
import com.skul9x.locateshare.util.SupabaseConnectionGuard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class CarActivity : AppCompatActivity() {

    internal lateinit var tvLocation: TextView
    private lateinit var tvLocationName: TextView
    private lateinit var btnOpenMap: Button

    private var currentUrl: String = ""

    private var _api: ApiService? = null
    internal var api: ApiService
        get() = _api ?: RetrofitClient.getApiService().also { _api = it }
        set(value) {
            _api = value
        }

    internal val connectionGuard = SupabaseConnectionGuard()
    internal var networkObserver: INetworkConnectivityObserver = NetworkConnectivityObserver(this)
    internal var favoritesDoubleTapHandler: DoubleTapHandler? = null
    internal var currentFavoritesDialog: Dialog? = null
    internal var ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
    internal var coroutineScope: kotlinx.coroutines.CoroutineScope? = null
    private val scope: kotlinx.coroutines.CoroutineScope
        get() = coroutineScope ?: lifecycleScope

    var hasAutoOpenedWifiOnFailure: Boolean
        get() = connectionGuard.hasAutoOpenedWifiOnFailure
        set(value) {
            connectionGuard.hasAutoOpenedWifiOnFailure = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car)

        val rootLayout = findViewById<ConstraintLayout>(R.id.rootCarLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                statusBarInsets.left,
                statusBarInsets.top,
                statusBarInsets.right,
                statusBarInsets.bottom
            )
            insets
        }

        tvLocation = findViewById(R.id.tvLocation)
        tvLocationName = findViewById(R.id.tvLocationName)
        btnOpenMap = findViewById(R.id.btnOpenMap)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnReload = findViewById<Button>(R.id.btnReload)
        val btnFavorites = findViewById<Button>(R.id.btnFavorites)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val btnWifiSettings = findViewById<ImageButton>(R.id.btnWifiSettings)

        // Back
        btnBack.setOnClickListener { finish() }

        // Wi-Fi Settings
        btnWifiSettings.setOnClickListener { openWifiSettings() }

        // Settings
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Reload: Fetch current location from Supabase
        btnReload.setOnClickListener {
            fetchCurrentLocation(isManualReload = true)
        }

        // Open Map: Mở URL hiện tại trên bản đồ
        btnOpenMap.setOnClickListener {
            if (currentUrl.isNotEmpty()) {
                openMap(currentUrl)
            } else {
                Toast.makeText(this, "Chưa có địa điểm nào!", Toast.LENGTH_SHORT).show()
            }
        }

        // Favorites: Double-Tap Handler (Single tap -> Starred, Double tap -> Popup)
        val tapHandler = favoritesDoubleTapHandler ?: initFavoritesDoubleTapHandler()
        btnFavorites.setOnClickListener(tapHandler)

        // Auto-load current location khi vào
        fetchCurrentLocation(isManualReload = false)
    }

    internal fun initFavoritesDoubleTapHandler(
        scheduler: DoubleTapHandler.Scheduler = DoubleTapHandler.DefaultScheduler(),
        timeProvider: () -> Long = { System.currentTimeMillis() }
    ): DoubleTapHandler {
        val handler = DoubleTapHandler(
            timeoutMs = DoubleTapHandler.DEFAULT_TIMEOUT_MS,
            timeProvider = timeProvider,
            scheduler = scheduler,
            onSingleTap = { openStarredFavorite() },
            onDoubleTap = { showFavoritesPopup() }
        )
        favoritesDoubleTapHandler = handler
        return handler
    }

    override fun onResume() {
        super.onResume()
        handleResume()
    }

    internal fun handleResume() {
        // Refresh khi quay lại từ Settings (throttled to avoid redundant fetch after auto-dismiss)
        if (!connectionGuard.shouldThrottleFetch(System.currentTimeMillis())) {
            fetchCurrentLocation(isManualReload = false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handleDestroy()
    }

    internal fun handleDestroy() {
        networkObserver.stopListening()
        favoritesDoubleTapHandler?.cancelPending()
        currentFavoritesDialog?.dismiss()
        currentFavoritesDialog = null
    }

    open fun fetchCurrentLocation(isManualReload: Boolean = false) {
        scope.launch {
            try {
                val locations = withContext(ioDispatcher) {
                    api.getCurrentLocation()
                }
                connectionGuard.onFetchSuccess()
                if (locations.isNotEmpty()) {
                    val loc = locations[0]
                    if (loc.url.isNotEmpty()) {
                        currentUrl = loc.url
                        updateUI(loc.url, loc.name)
                    } else {
                        tvLocation.text = "Chưa có địa điểm nào"
                        tvLocationName.text = ""
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val action = connectionGuard.handleFetchError(e, isManualReload)
                    action.locationMessage?.let { tvLocation.text = it }
                    try {
                        Toast.makeText(this@CarActivity, action.toastMessage, Toast.LENGTH_SHORT)?.show()
                    } catch (_: Throwable) {}
                    if (action.shouldOpenWifi) {
                        openWifiSettings()
                    }
                }
            }
        }
    }

    private fun updateUI(url: String, name: String) {
        tvLocation.text = url
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        tvLocationName.text = if (name.isNotEmpty()) {
            "📍 $name  •  🕒 $currentTime"
        } else {
            "🕒 Cập nhật: $currentTime"
        }
    }

    internal open fun openStarredFavorite() {
        scope.launch {
            try {
                val starred = withContext(ioDispatcher) {
                    api.getStarredFavorite()
                }
                if (starred.isNotEmpty()) {
                    openMap(starred[0].url)
                } else {
                    try {
                        Toast.makeText(this@CarActivity, EMPTY_STARRED_TOAST, Toast.LENGTH_LONG)?.show()
                    } catch (_: Throwable) {}
                }
            } catch (e: Exception) {
                try {
                    Toast.makeText(this@CarActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT)?.show()
                } catch (_: Throwable) {}
            }
        }
    }

    internal open fun showFavoritesPopup() {
        scope.launch {
            try {
                val favorites = withContext(ioDispatcher) {
                    api.getFavorites()
                }

                if (favorites.isEmpty()) {
                    try {
                        Toast.makeText(this@CarActivity, "Chưa có địa điểm ưa thích!\nVào ⚙️ Settings để thêm", Toast.LENGTH_LONG)?.show()
                    } catch (_: Throwable) {}
                    return@launch
                }

                val dialog = createFavoritesDialog(favorites)
                currentFavoritesDialog = dialog
                dialog.show()
            } catch (e: Exception) {
                try {
                    Toast.makeText(this@CarActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT)?.show()
                } catch (_: Throwable) {}
            }
        }
    }

    internal fun configureDialogWindow(
        window: android.view.Window?,
        density: Float = resources?.displayMetrics?.density ?: 1f,
        widthPixels: Int = resources?.displayMetrics?.widthPixels ?: 800,
        sdkInt: Int = Build.VERSION.SDK_INT
    ) {
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(POPUP_DIM_AMOUNT)
            if (sdkInt >= Build.VERSION_CODES.S) {
                addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                attributes = attributes.apply { blurBehindRadius = POPUP_BLUR_BEHIND_RADIUS }
            }
            val calculatedWidth = calculatePopupWidth(widthPixels, density)
            setLayout(
                calculatedWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    internal open fun createFavoritesDialog(favorites: List<FavoriteLocation>): Dialog {
        val dialog = Dialog(this, R.style.Theme_LocateShare_FloatingDialog)
        dialog.setContentView(R.layout.dialog_favorites_card_popup)

        configureDialogWindow(dialog.window)

        val rvFavoritesPopup = dialog.findViewById<RecyclerView>(R.id.rvFavoritesPopup)
        val btnClosePopup = dialog.findViewById<ImageButton>(R.id.btnClosePopup)

        val adapter = FavoriteCardAdapter(
            items = favorites.toMutableList(),
            onItemClick = { fav ->
                dialog.dismiss()
                openMap(fav.url)
            },
            onOpenMapClick = { fav ->
                dialog.dismiss()
                openMap(fav.url)
            }
        )

        rvFavoritesPopup?.apply {
            layoutManager = LinearLayoutManager(this@CarActivity)
            this.adapter = adapter
        }

        btnClosePopup?.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }

    internal open fun openMap(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage("com.google.android.apps.maps")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback to browser if Maps app not installed
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể mở bản đồ", Toast.LENGTH_SHORT).show()
        }
    }

    internal fun openWifiSettings(): Boolean {
        val opened = NetworkUtils.openWifiSettings(this)
        if (opened) {
            networkObserver.startListening { onInternetRestored() }
        } else {
            try {
                Toast.makeText(this, "Không thể mở cài đặt Wi-Fi", Toast.LENGTH_SHORT)?.show()
            } catch (_: Throwable) {}
        }
        return opened
    }

    internal fun createBringToFrontIntent(): Intent {
        return Intent(this, CarActivity::class.java).apply {
            flags = BRING_TO_FRONT_FLAGS
        }
    }

    internal fun onInternetRestored() {
        fetchCurrentLocation(isManualReload = false)
        try {
            val bringToFrontIntent = createBringToFrontIntent()
            startActivity(bringToFrontIntent)
        } catch (e: Exception) {
            // Safe fallback for custom ROMs
        }
        try {
            Toast.makeText(this, "Đã kết nối Internet! Đang cập nhật vị trí...", Toast.LENGTH_SHORT)?.show()
        } catch (_: Throwable) {}
        networkObserver.stopListening()
    }

    companion object {
        const val BRING_TO_FRONT_FLAGS = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
        const val EMPTY_STARRED_TOAST = "Chưa có địa điểm mặc định ⭐\nChạm đúp để chọn từ danh sách"
        const val POPUP_DIM_AMOUNT = 0.85f
        const val POPUP_BLUR_BEHIND_RADIUS = 60
        const val POPUP_MAX_WIDTH_DP = 640

        fun calculatePopupWidth(widthPixels: Int, density: Float): Int {
            val maxWidthPx = (POPUP_MAX_WIDTH_DP * density).toInt()
            return (widthPixels * 0.85).toInt().coerceAtMost(maxWidthPx)
        }
    }
}
