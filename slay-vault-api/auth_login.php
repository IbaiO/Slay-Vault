<?php
require_once __DIR__ . '/conexion.php';

$usuario = isset($_POST['usuario']) ? trim($_POST['usuario']) : '';
$password = isset($_POST['password']) ? trim($_POST['password']) : '';

if ($usuario === '' || $password === '') {
    echo json_encode([
        "success" => false,
        "message" => "usuario and password are required"
    ]);
    exit;
}

$stmt = mysqli_prepare($conexion, 'SELECT id, usuario, password_hash FROM usuarios WHERE usuario = ? LIMIT 1');
mysqli_stmt_bind_param($stmt, 's', $usuario);
mysqli_stmt_execute($stmt);
$result = mysqli_stmt_get_result($stmt);
$row = $result ? mysqli_fetch_assoc($result) : null;
mysqli_stmt_close($stmt);

if (!$row || !password_verify($password, $row['password_hash'])) {
    echo json_encode([
        "success" => false,
        "message" => "Invalid credentials"
    ]);
    exit;
}

echo json_encode([
    "success" => true,
    "message" => "Login ok",
    "user" => [
        "id" => $row['id'],
        "usuario" => $row['usuario']
    ]
]);

