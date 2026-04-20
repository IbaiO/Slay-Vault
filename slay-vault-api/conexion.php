<?php
header('Content-Type: application/json; charset=utf-8');

$DB_SERVER = getenv('DB_SERVER') ?: "localhost";
$DB_USER = getenv('DB_USER') ?: "root";
$DB_PASS = getenv('DB_PASS') ?: "";
$DB_DATABASE = getenv('DB_DATABASE') ?: "slay_vault";

$conexion = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);

if (!$conexion) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Database connection failed"
    ]);
    exit;
}

mysqli_set_charset($conexion, "utf8mb4");


function saveBase64ImageToUploads($imageBase64, $subfolder, $filePrefix) {
    $normalized = trim((string)$imageBase64);
    $normalized = preg_replace('/^data:image\/[a-zA-Z0-9.+-]+;base64,/', '', $normalized);
    $normalized = preg_replace('/\s+/', '', $normalized);
    if ($normalized === null || $normalized === '') {
        return null;
    }

    $binaryData = base64_decode($normalized, true);
    if ($binaryData === false) {
        return null;
    }

    $uploadsRoot = __DIR__ . '/uploads/' . trim($subfolder, '/');
    if (!is_dir($uploadsRoot) && !mkdir($uploadsRoot, 0775, true) && !is_dir($uploadsRoot)) {
        return null;
    }

    $safePrefix = preg_replace('/[^a-zA-Z0-9_-]/', '_', (string)$filePrefix);
    $fileName = $safePrefix . '_' . time() . '.jpg';
    $filePath = $uploadsRoot . '/' . $fileName;
    $fp = fopen($filePath, 'wb');
    if ($fp === false) {
        return null;
    }

    $written = fwrite($fp, $binaryData);
    fclose($fp);
    if ($written === false || $written <= 0) {
        return null;
    }

    $basePath = rtrim(dirname($_SERVER['SCRIPT_NAME']), '/');
    $publicPath = $basePath . '/uploads/' . trim($subfolder, '/') . '/' . rawurlencode($fileName);
    return (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off' ? 'https://' : 'http://')
        . $_SERVER['HTTP_HOST']
        . $publicPath;
}