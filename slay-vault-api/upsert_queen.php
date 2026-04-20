<?php
require_once __DIR__ . '/conexion.php';

$userId = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
$queenId = isset($_POST['id']) ? trim($_POST['id']) : '';
$name = isset($_POST['name']) ? trim($_POST['name']) : '';
$description = isset($_POST['description']) ? trim($_POST['description']) : '';
$photoUri = isset($_POST['photo_uri']) ? trim($_POST['photo_uri']) : '';
$envyLevel = isset($_POST['envy_level']) ? (float) $_POST['envy_level'] : 0.0;
$shadesCount = isset($_POST['shades_count']) ? (int) $_POST['shades_count'] : 0;
$lastShadeDate = isset($_POST['last_shade_date']) ? trim($_POST['last_shade_date']) : '';
$songIdRaw = isset($_POST['song_id']) ? trim($_POST['song_id']) : '';

if ($userId === '' || $queenId === '' || $name === '') {
    echo json_encode([
        "success" => false,
        "message" => "user_id, id and name are required"
    ]);
    exit;
}

if ($songIdRaw !== '' && !ctype_digit($songIdRaw)) {
    echo json_encode([
        "success" => false,
        "message" => "song_id must be numeric"
    ]);
    exit;
}

$ownerStmt = mysqli_prepare($conexion, 'SELECT user_id FROM queens WHERE id = ? LIMIT 1');
mysqli_stmt_bind_param($ownerStmt, 's', $queenId);
mysqli_stmt_execute($ownerStmt);
$ownerResult = mysqli_stmt_get_result($ownerStmt);
$ownerRow = $ownerResult ? mysqli_fetch_assoc($ownerResult) : null;
mysqli_stmt_close($ownerStmt);

if ($ownerRow && $ownerRow['user_id'] !== $userId) {
    echo json_encode([
        "success" => false,
        "message" => "Forbidden queen id"
    ]);
    exit;
}

if ($lastShadeDate === '') {
    $lastShadeDate = null;
}

$songId = $songIdRaw === '' ? null : (int) $songIdRaw;

$stmt = mysqli_prepare(
    $conexion,
    'INSERT INTO queens (id, user_id, name, description, photo_uri, envy_level, shades_count, last_shade_date, song_id)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
     ON DUPLICATE KEY UPDATE
       name = VALUES(name),
       description = VALUES(description),
       photo_uri = VALUES(photo_uri),
       envy_level = VALUES(envy_level),
       shades_count = VALUES(shades_count),
       last_shade_date = VALUES(last_shade_date),
       song_id = VALUES(song_id),
       updated_at = CURRENT_TIMESTAMP'
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
    'sssssdisi',
    $queenId,
    $userId,
    $name,
    $description,
    $photoUri,
    $envyLevel,
    $shadesCount,
    $lastShadeDate,
    $songId
);

$ok = mysqli_stmt_execute($stmt);
mysqli_stmt_close($stmt);

if (!$ok) {
    echo json_encode([
        "success" => false,
        "message" => "Could not upsert queen"
    ]);
    exit;
}

echo json_encode([
    "success" => true,
    "message" => "Queen upserted"
]);

