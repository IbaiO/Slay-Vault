<?php
require_once __DIR__ . '/conexion.php';

$userId = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
$shadeId = isset($_POST['id']) ? trim($_POST['id']) : '';
$queenId = isset($_POST['queen_id']) ? trim($_POST['queen_id']) : '';
$title = isset($_POST['title']) ? trim($_POST['title']) : '';
$description = isset($_POST['description']) ? trim($_POST['description']) : '';
$category = isset($_POST['category']) ? trim($_POST['category']) : 'General';
$intensity = isset($_POST['intensity']) ? (float) $_POST['intensity'] : 0.0;
$dateMs = isset($_POST['date_ms']) ? (int) $_POST['date_ms'] : 0;
$latitude = isset($_POST['latitude']) ? trim($_POST['latitude']) : '';
$longitude = isset($_POST['longitude']) ? trim($_POST['longitude']) : '';
$locationAddress = isset($_POST['location_address']) ? trim($_POST['location_address']) : '';

if ($latitude !== '' && !is_numeric($latitude)) {
    echo json_encode([
        "success" => false,
        "message" => "latitude must be numeric"
    ]);
    exit;
}

if ($longitude !== '' && !is_numeric($longitude)) {
    echo json_encode([
        "success" => false,
        "message" => "longitude must be numeric"
    ]);
    exit;
}

if ($userId === '' || $shadeId === '' || $queenId === '' || $title === '') {
    echo json_encode([
        "success" => false,
        "message" => "user_id, id, queen_id and title are required"
    ]);
    exit;
}

$queenCheck = mysqli_prepare($conexion, 'SELECT id FROM queens WHERE id = ? AND user_id = ? LIMIT 1');
mysqli_stmt_bind_param($queenCheck, 'ss', $queenId, $userId);
mysqli_stmt_execute($queenCheck);
$queenRes = mysqli_stmt_get_result($queenCheck);
$queenRow = $queenRes ? mysqli_fetch_assoc($queenRes) : null;
mysqli_stmt_close($queenCheck);

if (!$queenRow) {
    echo json_encode([
        "success" => false,
        "message" => "Queen does not belong to this user"
    ]);
    exit;
}

$ownerStmt = mysqli_prepare($conexion, 'SELECT user_id FROM shade_entries WHERE id = ? LIMIT 1');
mysqli_stmt_bind_param($ownerStmt, 's', $shadeId);
mysqli_stmt_execute($ownerStmt);
$ownerResult = mysqli_stmt_get_result($ownerStmt);
$ownerRow = $ownerResult ? mysqli_fetch_assoc($ownerResult) : null;
mysqli_stmt_close($ownerStmt);

if ($ownerRow && $ownerRow['user_id'] !== $userId) {
    echo json_encode([
        "success" => false,
        "message" => "Forbidden shade id"
    ]);
    exit;
}

$dateValue = null;
if ($dateMs > 0) {
    $dateValue = date('Y-m-d H:i:s', (int) floor($dateMs / 1000));
}

// Convertir strings vacíos a NULL para latitude y longitude
$latitudeValue = $latitude !== '' ? (double) $latitude : null;
$longitudeValue = $longitude !== '' ? (double) $longitude : null;
$locationAddressValue = $locationAddress !== '' ? $locationAddress : null;

$stmt = mysqli_prepare(
    $conexion,
    'INSERT INTO shade_entries (id, user_id, queen_id, title, description, category, intensity, date, latitude, longitude, location_address)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
     ON DUPLICATE KEY UPDATE
       queen_id = VALUES(queen_id),
       title = VALUES(title),
       description = VALUES(description),
       category = VALUES(category),
       intensity = VALUES(intensity),
       date = VALUES(date),
       latitude = VALUES(latitude),
       longitude = VALUES(longitude),
       location_address = VALUES(location_address)'
);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => "Could not prepare statement"
    ]);
    exit;
}

mysqli_stmt_bind_param(
    $stmt,
    'ssssssdsdds',
    $shadeId,
    $userId,
    $queenId,
    $title,
    $description,
    $category,
    $intensity,
    $dateValue,
    $latitudeValue,
    $longitudeValue,
    $locationAddressValue
);

$ok = mysqli_stmt_execute($stmt);
mysqli_stmt_close($stmt);

if (!$ok) {
    echo json_encode([
        "success" => false,
        "message" => "Could not upsert shade"
    ]);
    exit;
}

$statsStmt = mysqli_prepare(
    $conexion,
    'UPDATE queens q
     SET q.shades_count = (SELECT COUNT(*) FROM shade_entries s WHERE s.queen_id = q.id),
         q.last_shade_date = (
             SELECT DATE_FORMAT(MAX(s2.date), "%d/%m/%Y")
             FROM shade_entries s2
             WHERE s2.queen_id = q.id
         ),
         q.updated_at = CURRENT_TIMESTAMP
     WHERE q.id = ? AND q.user_id = ?'
);
mysqli_stmt_bind_param($statsStmt, 'ss', $queenId, $userId);
mysqli_stmt_execute($statsStmt);
mysqli_stmt_close($statsStmt);

echo json_encode([
    "success" => true,
    "message" => "Shade upserted"
]);

