<?php
require_once __DIR__ . '/conexion.php';

$userId = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
$usuario = isset($_POST['usuario']) ? trim($_POST['usuario']) : '';

if ($userId === '' || $usuario === '') {
    echo json_encode([
        "success" => false,
        "message" => "user_id and usuario are required"
    ]);
    exit;
}

$loadStmt = mysqli_prepare($conexion, 'SELECT id FROM usuarios WHERE id = ? LIMIT 1');
if (!$loadStmt) {
    echo json_encode([
        "success" => false,
        "message" => "Database error while loading user"
    ]);
    exit;
}

mysqli_stmt_bind_param($loadStmt, 's', $userId);
mysqli_stmt_execute($loadStmt);
$loadResult = mysqli_stmt_get_result($loadStmt);
$user = $loadResult ? mysqli_fetch_assoc($loadResult) : null;
mysqli_stmt_close($loadStmt);

if (!$user) {
    echo json_encode([
        "success" => false,
        "message" => "User not found"
    ]);
    exit;
}

$checkStmt = mysqli_prepare($conexion, 'SELECT id FROM usuarios WHERE usuario = ? AND id <> ? LIMIT 1');
if (!$checkStmt) {
    echo json_encode([
        "success" => false,
        "message" => "Database error while checking username"
    ]);
    exit;
}

mysqli_stmt_bind_param($checkStmt, 'ss', $usuario, $userId);
mysqli_stmt_execute($checkStmt);
$checkResult = mysqli_stmt_get_result($checkStmt);
$existingUser = $checkResult ? mysqli_fetch_assoc($checkResult) : null;
mysqli_stmt_close($checkStmt);

if ($existingUser) {
    http_response_code(409);
    echo json_encode([
        "success" => false,
        "message" => "User already exists"
    ]);
    exit;
}

$updateStmt = mysqli_prepare($conexion, 'UPDATE usuarios SET usuario = ? WHERE id = ?');
if (!$updateStmt) {
    echo json_encode([
        "success" => false,
        "message" => "Database error while updating profile"
    ]);
    exit;
}

mysqli_stmt_bind_param($updateStmt, 'ss', $usuario, $userId);
$ok = mysqli_stmt_execute($updateStmt);
$updateErrorCode = mysqli_errno($conexion);
mysqli_stmt_close($updateStmt);

if (!$ok) {
    if ($updateErrorCode === 1062) {
        http_response_code(409);
        echo json_encode([
            "success" => false,
            "message" => "User already exists"
        ]);
        exit;
    }
    echo json_encode([
        "success" => false,
        "message" => "Could not update profile"
    ]);
    exit;
}

echo json_encode([
    "success" => true,
    "message" => "Profile updated",
    "user" => [
        "id" => $userId,
        "usuario" => $usuario
    ]
]);

