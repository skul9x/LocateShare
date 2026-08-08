package com.skul9x.locateshare.util

/**
 * Controller managing Supabase connection failure states and Wi-Fi auto-launch decisions.
 * Prevents infinite activity-switching loops when returning to the app without internet.
 */
class SupabaseConnectionGuard(
    var hasAutoOpenedWifiOnFailure: Boolean = false
) {
    data class FailureAction(
        val shouldOpenWifi: Boolean,
        val locationMessage: String?,
        val toastMessage: String
    )

    /**
     * Resets the failure guard when a Supabase API call succeeds.
     */
    fun onFetchSuccess() {
        hasAutoOpenedWifiOnFailure = false
    }

    /**
     * Evaluates a throwable and determines the appropriate UI and intent trigger actions.
     *
     * @param e The exception caught during fetch
     * @param isManualReload True if the fetch was triggered manually by the user
     */
    fun handleFetchError(e: Throwable, isManualReload: Boolean = false): FailureAction {
        return if (NetworkUtils.isConnectionFailure(e)) {
            val locationMsg = "⚠️ Không có kết nối mạng (Lỗi kết nối Supabase)"
            if (isManualReload) {
                FailureAction(
                    shouldOpenWifi = true,
                    locationMessage = locationMsg,
                    toastMessage = "Không thể kết nối đến máy chủ Supabase. Đang mở cài đặt Wi-Fi..."
                )
            } else if (!hasAutoOpenedWifiOnFailure) {
                hasAutoOpenedWifiOnFailure = true
                FailureAction(
                    shouldOpenWifi = true,
                    locationMessage = locationMsg,
                    toastMessage = "Không thể kết nối đến máy chủ Supabase. Đang mở cài đặt Wi-Fi..."
                )
            } else {
                FailureAction(
                    shouldOpenWifi = false,
                    locationMessage = locationMsg,
                    toastMessage = "Lỗi kết nối máy chủ: ${e.message ?: "Không xác định"}"
                )
            }
        } else {
            FailureAction(
                shouldOpenWifi = false,
                locationMessage = null,
                toastMessage = "Lỗi tải: ${e.message ?: "Không xác định"}"
            )
        }
    }
}
