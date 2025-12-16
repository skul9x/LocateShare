<?php
// index.php
// LocateShare Backend - Hybrid (UI + API)

// Disable error reporting to prevent PHP warnings from breaking JSON
error_reporting(0);

// Helper to check if request is from App
function isApp()
{
    // Check for custom header or POST request (API mode)
    if (isset($_SERVER['HTTP_X_REQUESTED_WITH']) && $_SERVER['HTTP_X_REQUESTED_WITH'] === 'com.skul9x.locateshare') {
        return true;
    }
    // Also treat standard POST as API for compatibility
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        return true;
    }
    return false;
}

$file = 'location.txt';

// --- API LOGIC ---
if (isApp()) {
    header('Content-Type: application/json; charset=utf-8');
    header('Access-Control-Allow-Origin: *');

    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $url = '';
        $name = '';

        if (isset($_POST['url'])) {
            $url = $_POST['url'];
            $name = $_POST['name'] ?? '';
        } else {
            $input = file_get_contents('php://input');
            $data = json_decode($input, true);
            if (isset($data['url'])) {
                $url = $data['url'];
                $name = $data['name'] ?? '';
            }
        }

        if (!empty($url)) {
            $saveData = ['url' => $url, 'name' => $name, 'updated' => time()];
            file_put_contents($file, json_encode($saveData));
            echo json_encode(['status' => 'success', 'message' => 'Location updated']);
        } else {
            http_response_code(400);
            echo json_encode(['status' => 'error', 'message' => 'No URL provided.']);
        }
    } else if ($_SERVER['REQUEST_METHOD'] === 'GET') {
        if (file_exists($file)) {
            echo file_get_contents($file);
        } else {
            echo json_encode(['url' => '', 'name' => '']);
        }
    }
    exit();
}

// --- UI LOGIC (Browser) ---
$currentData = ['url' => '', 'name' => ''];
if (file_exists($file)) {
    $content = file_get_contents($file);
    $json = json_decode($content, true);
    if ($json) {
        $currentData = $json;
    } else {
        $currentData['url'] = $content; // Legacy support
    }
}
?>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>LocateShare Dashboard</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Inter', sans-serif;
        }

        .glass {
            background: rgba(255, 255, 255, 0.7);
            backdrop-filter: blur(10px);
            -webkit-backdrop-filter: blur(10px);
        }
    </style>
</head>

<body class="bg-gradient-to-br from-blue-50 to-indigo-100 min-h-screen flex items-center justify-center p-4">

    <div
        class="max-w-md w-full bg-white rounded-2xl shadow-xl overflow-hidden transform transition-all hover:shadow-2xl duration-300">
        <!-- Header -->
        <div class="bg-indigo-600 p-6 text-center">
            <h1 class="text-2xl font-bold text-white tracking-wide">LocateShare</h1>
            <p class="text-indigo-200 text-sm mt-1">Chia sẻ địa điểm nhanh chóng</p>
        </div>

        <!-- Content -->
        <div class="p-6 space-y-6">

            <!-- Current Location Card -->
            <div class="bg-indigo-50 rounded-xl p-4 border border-indigo-100">
                <p class="text-xs font-semibold text-indigo-500 uppercase tracking-wider mb-2">Địa điểm hiện tại</p>
                <div class="flex items-start space-x-3">
                    <div class="flex-shrink-0 mt-1">
                        <svg class="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z">
                            </path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
                        </svg>
                    </div>
                    <div class="overflow-hidden">
                        <a href="<?php echo htmlspecialchars($currentData['url']); ?>" target="_blank"
                            class="text-indigo-700 font-medium hover:underline truncate block">
                            <?php echo $currentData['url'] ? htmlspecialchars($currentData['url']) : 'Chưa có dữ liệu'; ?>
                        </a>
                        <?php if ($currentData['updated']): ?>
                            <p class="text-xs text-gray-400 mt-1">Cập nhật:
                                <?php echo date('H:i:s d/m', $currentData['updated']); ?>
                            </p>
                        <?php endif; ?>
                    </div>
                </div>
            </div>

            <!-- Input Area -->
            <div class="space-y-4">
                <label class="block text-sm font-medium text-gray-700">Dán Link Google Maps</label>
                <div class="relative">
                    <input type="text" id="urlInput"
                        class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all outline-none pl-10 pr-24"
                        placeholder="https://maps.app.goo.gl/...">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1">
                            </path>
                        </svg>
                    </div>
                    <button onclick="pasteLink()"
                        class="absolute inset-y-0 right-0 px-4 text-sm font-medium text-indigo-600 hover:text-indigo-800 transition-colors">
                        PASTE
                    </button>
                </div>
                <p id="status" class="text-xs text-gray-500 h-4 transition-all"></p>
            </div>

        </div>

        <!-- Footer -->
        <div class="bg-gray-50 px-6 py-4 border-t border-gray-100 flex justify-between items-center">
            <span class="text-xs text-gray-400">v2.0 Hybrid</span>
            <div id="loading" class="hidden">
                <svg class="animate-spin h-5 w-5 text-indigo-600" xmlns="http://www.w3.org/2000/svg" fill="none"
                    viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                    </path>
                </svg>
            </div>
        </div>
    </div>

    <script>
        const urlInput = document.getElementById('urlInput');
        const status = document.getElementById('status');
        const loading = document.getElementById('loading');
        let isSaving = false;
        let pendingUrl = '';

        // Auto-save logic
        urlInput.addEventListener('input', function () {
            const url = this.value.trim();
            if (url && url !== pendingUrl) {
                if (isValidGoogleMapsLink(url)) {
                    saveLink(url);
                } else {
                    status.textContent = 'Link không hợp lệ! Phải là link Google Maps.';
                    status.className = 'text-xs text-red-500 h-4 transition-all';
                }
            }
        });

        async function pasteLink() {
            try {
                const text = await navigator.clipboard.readText();
                urlInput.value = text;
                if (isValidGoogleMapsLink(text)) {
                    saveLink(text);
                } else {
                    status.textContent = 'Link vừa dán không phải là link Google Maps!';
                    status.className = 'text-xs text-red-500 h-4 transition-all';
                }
            } catch (err) {
                status.textContent = 'Không thể truy cập clipboard. Hãy dán thủ công.';
                status.className = 'text-xs text-red-500 h-4 transition-all';
            }
        }

        function isValidGoogleMapsLink(url) {
            // Regex to match common Google Maps domains
            const regex = /^(https?:\/\/)?(www\.)?(google\.com\/maps|maps\.google\.com|maps\.app\.goo\.gl|goo\.gl\/maps)/;
            return regex.test(url);
        }

        async function saveLink(url) {
            if (!url) return;

            isSaving = true;
            pendingUrl = url;
            status.textContent = 'Đang lưu...';
            status.className = 'text-xs text-blue-500 h-4 transition-all';
            loading.classList.remove('hidden');

            try {
                const formData = new FormData();
                formData.append('url', url);
                formData.append('name', ''); // Web UI doesn't fetch name

                const response = await fetch('index.php', {
                    method: 'POST',
                    body: formData
                });

                const result = await response.json();

                if (result.status === 'success') {
                    status.textContent = 'Đã lưu thành công!';
                    status.className = 'text-xs text-green-500 h-4 transition-all';
                    isSaving = false;
                    pendingUrl = '';

                    // Optional: Reload page to update "Current Location" card
                    setTimeout(() => location.reload(), 1000);
                } else {
                    throw new Error(result.message);
                }
            } catch (error) {
                console.error('Error:', error);
                status.textContent = 'Lỗi khi lưu. Đang thử lại...';
                status.className = 'text-xs text-red-500 h-4 transition-all';
                // Retry logic could go here, but for now we just leave isSaving true
            } finally {
                loading.classList.add('hidden');
            }
        }

        // Window close warning
        window.addEventListener('beforeunload', function (e) {
            if (isSaving) {
                e.preventDefault();
                e.returnValue = 'Link chưa được lưu, bạn có muốn đóng cửa sổ không?';
            }
        });
    </script>
</body>

</html>