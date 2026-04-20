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

$check = mysqli_prepare($conexion, 'SELECT id FROM usuarios WHERE usuario = ? LIMIT 1');
if (!$check) {
    echo json_encode([
        "success" => false,
        "message" => "Database error while checking username"
    ]);
    exit;
}
mysqli_stmt_bind_param($check, 's', $usuario);
mysqli_stmt_execute($check);
mysqli_stmt_store_result($check);

if (mysqli_stmt_num_rows($check) > 0) {
    http_response_code(409);
    echo json_encode([
        "success" => false,
        "message" => "User already exists"
    ]);
    mysqli_stmt_close($check);
    exit;
}
mysqli_stmt_close($check);

$userId = uniqid('u_', true);
$passwordHash = password_hash($password, PASSWORD_DEFAULT);
$insert = mysqli_prepare($conexion, 'INSERT INTO usuarios (id, usuario, password_hash) VALUES (?, ?, ?)');
if (!$insert) {
    echo json_encode([
        "success" => false,
        "message" => "Database error while creating user"
    ]);
    exit;
}
mysqli_stmt_bind_param($insert, 'sss', $userId, $usuario, $passwordHash);
$ok = mysqli_stmt_execute($insert);
$insertErrorCode = mysqli_errno($conexion);
mysqli_stmt_close($insert);

if (!$ok) {
    if ($insertErrorCode === 1062) {
        http_response_code(409);
        echo json_encode([
            "success" => false,
            "message" => "User already exists"
        ]);
        exit;
    }
    echo json_encode([
        "success" => false,
        "message" => "Could not create user"
    ]);
    exit;
}

echo json_encode([
    "success" => true,
    "message" => "User created",
    "user" => [
        "id" => $userId,
        "usuario" => $usuario
    ]
]);

