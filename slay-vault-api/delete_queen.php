<?php
require_once __DIR__ . '/conexion.php';

$userId = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
$queenId = isset($_POST['queen_id']) ? trim($_POST['queen_id']) : '';

if ($userId === '' || $queenId === '') {
    echo json_encode([
        "success" => false,
        "message" => "user_id and queen_id are required"
    ]);
    exit;
}

mysqli_begin_transaction($conexion);

try {
    $deleteShades = mysqli_prepare($conexion, 'DELETE FROM shade_entries WHERE queen_id = ? AND user_id = ?');
    mysqli_stmt_bind_param($deleteShades, 'ss', $queenId, $userId);
    mysqli_stmt_execute($deleteShades);
    mysqli_stmt_close($deleteShades);

    $deleteQueen = mysqli_prepare($conexion, 'DELETE FROM queens WHERE id = ? AND user_id = ?');
    mysqli_stmt_bind_param($deleteQueen, 'ss', $queenId, $userId);
    mysqli_stmt_execute($deleteQueen);
    $affected = mysqli_stmt_affected_rows($deleteQueen);
    mysqli_stmt_close($deleteQueen);

    if ($affected <= 0) {
        mysqli_rollback($conexion);
        echo json_encode([
            "success" => false,
            "message" => "Queen not found"
        ]);
        exit;
    }

    mysqli_commit($conexion);

    echo json_encode([
        "success" => true,
        "message" => "Queen deleted"
    ]);
} catch (Exception $e) {
    mysqli_rollback($conexion);
    echo json_encode([
        "success" => false,
        "message" => "Could not delete queen"
    ]);
}

