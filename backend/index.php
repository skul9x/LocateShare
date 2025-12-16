<?php
// index.php
// LocateShare Backend - Robust Version

// Disable error reporting to prevent PHP warnings from breaking JSON
error_reporting(0);

// Set JSON header
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST');

$file = 'location.txt';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $url = '';
    $name = '';

    // Try to get from standard POST (Form Data)
    if (isset($_POST['url'])) {
        $url = $_POST['url'];
        $name = $_POST['name'] ?? '';
    }
    // Try to get from JSON Body
    else {
        $input = file_get_contents('php://input');
        $data = json_decode($input, true);
        if (isset($data['url'])) {
            $url = $data['url'];
            $name = $data['name'] ?? '';
        }
    }

    if (!empty($url)) {
        // Save as JSON object
        $saveData = ['url' => $url, 'name' => $name];
        file_put_contents($file, json_encode($saveData));
        echo json_encode(['status' => 'success', 'message' => 'Location updated']);
    } else {
        http_response_code(400);
        echo json_encode(['status' => 'error', 'message' => 'No URL provided.']);
    }
} else if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    if (file_exists($file)) {
        $content = file_get_contents($file);

        // Check if content is already JSON (new format)
        $json = json_decode($content, true);
        if ($json && isset($json['url'])) {
            echo $content; // Return stored JSON directly
        } else {
            // Legacy format (raw URL in file)
            echo json_encode(['url' => $content, 'name' => '']);
        }
    } else {
        echo json_encode(['url' => '', 'name' => '']);
    }
} else {
    http_response_code(405);
    echo json_encode(['status' => 'error', 'message' => 'Method not allowed']);
}

// CRITICAL: Exit to prevent free hosting from injecting HTML footers
exit();
?>