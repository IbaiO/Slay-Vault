<?php
require_once __DIR__ . '/conexion.php';

$userId = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
$queenId = isset($_POST['queen_id']) ? trim($_POST['queen_id']) : '';
$imageBase64 = isset($_POST['image_base64']) ? trim($_POST['image_base64']) : '';

if ($userId === '' || $queenId === '' || $imageBase64 === '') {
    echo json_encode([
        "success" => false,
        "message" => "user_id, queen_id and image_base64 are required"
    ]);
    exit;
}

$binaryData = base64_decode($imageBase64, true);
if ($binaryData === false) {
    echo json_encode([
        "success" => false,
        "message" => "Invalid base64 payload"
    ]);
    exit;
}

$uploadsRoot = __DIR__ . '/uploads/queens/' . $userId;
if (!is_dir($uploadsRoot)) {
    mkdir($uploadsRoot, 0775, true);
}

$fileName = $queenId . '_' . time() . '.jpg';
$filePath = $uploadsRoot . '/' . $fileName;

$fp = fopen($filePath, 'wb');
if ($fp === false) {
    echo json_encode([
        "success" => false,
        "message" => "Could not create destination file"
    ]);
    exit;
}

$written = fwrite($fp, $binaryData);
fclose($fp);

if ($written === false || $written <= 0) {
    echo json_encode([
        "success" => false,
        "message" => "Could not write image data"
    ]);
    exit;
}

$basePath = rtrim(dirname($_SERVER['SCRIPT_NAME']), '/');
$publicPath = $basePath . '/uploads/queens/' . rawurlencode($userId) . '/' . rawurlencode($fileName);
$photoUrl = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off' ? 'https://' : 'http://')
    . $_SERVER['HTTP_HOST']
    . $publicPath;

$update = mysqli_prepare($conexion, 'UPDATE queens SET photo_uri = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?');
if ($update) {
    mysqli_stmt_bind_param($update, 'sss', $photoUrl, $queenId, $userId);
    mysqli_stmt_execute($update);
    mysqli_stmt_close($update);
}

echo json_encode([
    "success" => true,
    "message" => "Photo uploaded",
    "photo_url" => $photoUrl
]);

