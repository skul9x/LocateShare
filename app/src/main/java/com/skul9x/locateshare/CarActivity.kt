package com.skul9x.locateshare

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skul9x.locateshare.adapter.FavoriteCardAdapter
import com.skul9x.locateshare.adapter.QuickFavoriteAdapter
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
    internal lateinit var tvLocationName: TextView
    internal lateinit var btnOpenMap: Button
    internal var tvSyncHeader: TextView? = null
    internal var tvSyncTime: TextView? = null
    internal var ivSyncStatus: ImageView? = null
    internal var quickFavoriteAdapter: QuickFavoriteAdapter? = null

    var currentUrl: String = ""
        internal set

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

        val rootLayout = findViewById<View>(R.id.rootCarLayout) ?: findViewById<View>(android.R.id.content)
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
                val systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                )
                view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )
                insets
            }
        }

        tvLocation = findViewById(R.id.tvLocation)
        tvLocationName = findViewById(R.id.tvLocationName)
        btnOpenMap = findViewById(R.id.btnOpenMap)
        tvSyncHeader = findViewById(R.id.tvSyncHeader)
        tvSyncTime = findViewById(R.id.tvSyncTime)
        ivSyncStatus = findViewById(R.id.ivSyncStatus)

        val btnBack = findViewById<View?>(R.id.btnBack)
        val btnReload = findViewById<View?>(R.id.btnReload)
        val btnFavorites = findViewById<View?>(R.id.btnFavorites)
        val btnStarredQuick = findViewById<View?>(R.id.btnStarredQuick)
        val btnSettings = findViewById<View?>(R.id.btnSettings)
        val btnWifiSettings = findViewById<View?>(R.id.btnWifiSettings)
        val rvQuickFavorites = findViewById<RecyclerView?>(R.id.rvQuickFavorites)

        // Setup Quick Favorites RecyclerView (Landscape layout)
        if (rvQuickFavorites != null) {
            val adapter = QuickFavoriteAdapter(
                onItemClick = { fav ->
                    openMap(fav.url)
                }
            )
            quickFavoriteAdapter = adapter
            rvQuickFavorites.layoutManager = LinearLayoutManager(this)
            rvQuickFavorites.adapter = adapter
        }

        // Back
        btnBack?.setOnClickListener { finish() }

        // Wi-Fi Settings
        btnWifiSettings?.setOnClickListener { openWifiSettings() }

        // Settings
        btnSettings?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Starred Quick Button (Driver Rail direct 1-tap launcher)
        btnStarredQuick?.setOnClickListener {
            openStarredFavorite()
        }

        // Reload: Fetch current location from Supabase
        btnReload?.setOnClickListener {
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
        btnFavorites?.setOnClickListener(tapHandler)

        // Auto-load current location and quick favorites
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
                updateSyncStatus(isConnected = false, isSyncing = true)
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
                        if (::tvLocation.isInitialized) tvLocation.text = "Chưa có địa điểm nào"
                        if (::tvLocationName.isInitialized) tvLocationName.text = ""
                        updateSyncStatus(isConnected = true, isSyncing = false)
                    }
                } else {
                    updateSyncStatus(isConnected = true, isSyncing = false)
                }
                fetchQuickFavorites()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateSyncStatus(isConnected = false, isSyncing = false)
                    val action = connectionGuard.handleFetchError(e, isManualReload)
                    action.locationMessage?.let { msg ->
                        if (::tvLocation.isInitialized) tvLocation.text = msg
                    }
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

    open fun fetchQuickFavorites() {
        scope.launch {
            try {
                val favorites = withContext(ioDispatcher) {
                    api.getFavorites()
                }
                quickFavoriteAdapter?.updateList(favorites)
            } catch (_: Exception) {
                // Safe ignore for quick list background sync
            }
        }
    }

    internal fun updateSyncStatus(isConnected: Boolean, isSyncing: Boolean = false) {
        val statusView = ivSyncStatus
        val colorRes = when {
            isSyncing -> R.color.car_status_syncing
            isConnected -> R.color.car_accent_emerald_bright
            else -> R.color.car_status_disconnected
        }
        try {
            statusView?.setColorFilter(ContextCompat.getColor(this, colorRes))
            tvSyncHeader?.let { header ->
                val color = ContextCompat.getColor(this, colorRes)
                header.setTextColor(color)
                if (isSyncing) {
                    header.text = "● ĐANG ĐỒNG BỘ..."
                } else if (isConnected) {
                    header.text = "● ĐÃ ĐỒNG BỘ TỪ ĐIỆN THOẠI"
                } else {
                    header.text = "● MẤT KẾT NỐI INTERNET"
                }
            }
        } catch (_: Throwable) {}
    }

    private fun updateUI(url: String, name: String) {
        if (::tvLocation.isInitialized) {
            tvLocation.text = url
        }
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        if (::tvLocationName.isInitialized) {
            tvLocationName.text = if (name.isNotEmpty()) {
                "📍 $name  •  🕒 $currentTime"
            } else {
                "🕒 Cập nhật: $currentTime"
            }
        }
        tvSyncTime?.text = "  •  $currentTime"
        updateSyncStatus(isConnected = true, isSyncing = false)
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
        sdkInt: Int = Build.VERSION.SDK_INT,
        isLandscape: Boolean = try {
            resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE
        } catch (_: Throwable) {
            false
        }
    ) {
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(POPUP_DIM_AMOUNT)
            if (sdkInt >= Build.VERSION_CODES.S) {
                addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                attributes = attributes.apply { blurBehindRadius = POPUP_BLUR_BEHIND_RADIUS }
            }
            val maxDp = if (isLandscape) POPUP_MAX_WIDTH_LAND_DP else POPUP_MAX_WIDTH_DP
            val calculatedWidth = calculatePopupWidth(widthPixels, density, maxDp)
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
        val tvEmptyFavorites = dialog.findViewById<TextView>(R.id.tvEmptyFavorites)

        if (favorites.isEmpty()) {
            tvEmptyFavorites?.visibility = View.VISIBLE
            rvFavoritesPopup?.visibility = View.GONE
        } else {
            tvEmptyFavorites?.visibility = View.GONE
            rvFavoritesPopup?.visibility = View.VISIBLE
        }

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

        val isLandscape = try {
            resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE
        } catch (_: Throwable) {
            false
        }

        rvFavoritesPopup?.apply {
            layoutManager = if (isLandscape) {
                GridLayoutManager(this@CarActivity, 2)
            } else {
                LinearLayoutManager(this@CarActivity)
            }
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
        const val POPUP_MAX_WIDTH_LAND_DP = 1100

        fun calculatePopupWidth(
            widthPixels: Int,
            density: Float,
            maxWidthDp: Int = POPUP_MAX_WIDTH_DP
        ): Int {
            val maxWidthPx = (maxWidthDp * density).toInt()
            return (widthPixels * 0.85).toInt().coerceAtMost(maxWidthPx)
        }
    }
}
