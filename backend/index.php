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

    // Try to get from standard POST (Form Data)
    if (isset($_POST['url'])) {
        $url = $_POST['url'];
    }
    // Try to get from JSON Body
    else {
        $input = file_get_contents('php://input');
        $data = json_decode($input, true);
        if (isset($data['url'])) {
            $url = $data['url'];
        }
    }

    if (!empty($url)) {
        file_put_contents($file, $url);
        echo json_encode(['status' => 'success', 'message' => 'Location updated']);
    } else {
        http_response_code(400);
        echo json_encode(['status' => 'error', 'message' => 'No URL provided. Received: ' . print_r($_POST, true)]);
    }
} else if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    if (file_exists($file)) {
        $url = file_get_contents($file);
        echo json_encode(['url' => $url]);
    } else {
        echo json_encode(['url' => '']);
    }
} else {
    http_response_code(405);
    echo json_encode(['status' => 'error', 'message' => 'Method not allowed']);
}

// CRITICAL: Exit to prevent free hosting from injecting HTML footers
exit();
?>