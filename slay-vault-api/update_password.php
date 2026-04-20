<?php
require_once __DIR__ . '/conexion.php';

$userId = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
$currentPassword = isset($_POST['current_password']) ? trim($_POST['current_password']) : '';
$newPassword = isset($_POST['new_password']) ? trim($_POST['new_password']) : '';

if ($userId === '' || $currentPassword === '' || $newPassword === '') {
    echo json_encode([
        "success" => false,
        "message" => "user_id, current_password and new_password are required"
    ]);
    exit;
}


$loadStmt = mysqli_prepare($conexion, 'SELECT id, usuario, password_hash FROM usuarios WHERE id = ? LIMIT 1');
if (!$loadStmt) {
    echo json_encode([
        "success" => false,
        "message" => "Database error while loading user"
    ]);
    exit;
}
mysqli_stmt_bind_param($loadStmt, 's', $userId);
mysqli_stmt_execute($loadStmt);
$result = mysqli_stmt_get_result($loadStmt);
$user = $result ? mysqli_fetch_assoc($result) : null;
mysqli_stmt_close($loadStmt);

if (!$user) {
    echo json_encode([
        "success" => false,
        "message" => "User not found"
    ]);
    exit;
}

if (!password_verify($currentPassword, $user['password_hash'])) {
    echo json_encode([
        "success" => false,
        "message" => "Current password is incorrect"
    ]);
    exit;
}

$newPasswordHash = password_hash($newPassword, PASSWORD_DEFAULT);
$updateStmt = mysqli_prepare($conexion, 'UPDATE usuarios SET password_hash = ? WHERE id = ?');
if (!$updateStmt) {
    echo json_encode([
        "success" => false,
        "message" => "Database error while updating password"
    ]);
    exit;
}

mysqli_stmt_bind_param($updateStmt, 'ss', $newPasswordHash, $userId);
$ok = mysqli_stmt_execute($updateStmt);
mysqli_stmt_close($updateStmt);

if (!$ok) {
    echo json_encode([
        "success" => false,
        "message" => "Could not update password"
    ]);
    exit;
}

echo json_encode([
    "success" => true,
    "message" => "Password updated",
    "user" => [
        "id" => $user['id'],
        "usuario" => $user['usuario']
    ]
]);

