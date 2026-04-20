<?php
require_once __DIR__ . '/conexion.php';

$userId = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
$shadeId = isset($_POST['shade_id']) ? trim($_POST['shade_id']) : '';

if ($userId === '' || $shadeId === '') {
    echo json_encode([
        "success" => false,
        "message" => "user_id and shade_id are required"
    ]);
    exit;
}

$queenStmt = mysqli_prepare($conexion, 'SELECT queen_id FROM shade_entries WHERE id = ? AND user_id = ? LIMIT 1');
mysqli_stmt_bind_param($queenStmt, 'ss', $shadeId, $userId);
mysqli_stmt_execute($queenStmt);
$queenResult = mysqli_stmt_get_result($queenStmt);
$shadeRow = $queenResult ? mysqli_fetch_assoc($queenResult) : null;
mysqli_stmt_close($queenStmt);

if (!$shadeRow) {
    echo json_encode([
        "success" => false,
        "message" => "Shade not found"
    ]);
    exit;
}

$queenId = $shadeRow['queen_id'];

$deleteStmt = mysqli_prepare($conexion, 'DELETE FROM shade_entries WHERE id = ? AND user_id = ?');
mysqli_stmt_bind_param($deleteStmt, 'ss', $shadeId, $userId);
mysqli_stmt_execute($deleteStmt);
$affected = mysqli_stmt_affected_rows($deleteStmt);
mysqli_stmt_close($deleteStmt);

if ($affected <= 0) {
    echo json_encode([
        "success" => false,
        "message" => "Shade not deleted"
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
    "message" => "Shade deleted"
]);

