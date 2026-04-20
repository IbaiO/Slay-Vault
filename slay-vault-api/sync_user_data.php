<?php
require_once __DIR__ . '/conexion.php';

$userId = isset($_GET['usuario']) ? trim($_GET['usuario']) : '';

if ($userId === '') {
    echo json_encode([
        "success" => false,
        "message" => "usuario is required"
    ]);
    exit;
}

$queens = [];
$shades = [];

$qStmt = mysqli_prepare($conexion, 'SELECT id, user_id, name, description, photo_uri, envy_level, shades_count, last_shade_date, song_id FROM queens WHERE user_id = ? ORDER BY updated_at DESC');
mysqli_stmt_bind_param($qStmt, 's', $userId);
mysqli_stmt_execute($qStmt);
$qResult = mysqli_stmt_get_result($qStmt);
while ($row = mysqli_fetch_assoc($qResult)) {
    $queens[] = $row;
}
mysqli_stmt_close($qStmt);

$sStmt = mysqli_prepare($conexion, 'SELECT id, user_id, queen_id, title, description, category, intensity, date, latitude, longitude, location_address FROM shade_entries WHERE user_id = ? ORDER BY date DESC');
mysqli_stmt_bind_param($sStmt, 's', $userId);
mysqli_stmt_execute($sStmt);
$sResult = mysqli_stmt_get_result($sStmt);
while ($row = mysqli_fetch_assoc($sResult)) {
    $shades[] = $row;
}
mysqli_stmt_close($sStmt);

echo json_encode([
    "success" => true,
    "message" => "Sync payload ready",
    "queens" => $queens,
    "shades" => $shades
]);

