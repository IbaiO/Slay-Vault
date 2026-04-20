<?php
require_once __DIR__ . '/conexion.php';

function respondJson($statusCode, $success, $message, $extra = []) {
    http_response_code($statusCode);
    echo json_encode(array_merge([
        'success' => $success,
        'message' => $message,
    ], $extra));
    exit;
}

try {
    $mensaje = isset($_POST['mensaje']) ? trim($_POST['mensaje']) : '';
    $usuario = isset($_POST['usuario']) ? trim($_POST['usuario']) : '';

    if ($mensaje === '') {
        respondJson(400, false, 'mensaje is required');
    }

    if (strlen($mensaje) > 280) {
        respondJson(400, false, 'mensaje is too long');
    }

    $serverKey = getenv('FCM_SERVER_KEY');
    if ($serverKey === false || trim($serverKey) === '') {
        respondJson(500, false, 'FCM server key is not configured');
    }

    if ($usuario === '') {
        $usuario = 'Usuario';
    }

    $title = '¡Grito de Guerra!';
    $body = $usuario . ': ' . $mensaje;

    $payload = [
        'to' => '/topics/divas_global',
        'notification' => [
            'title' => $title,
            'body' => $body,
        ],
        'data' => [
            'title' => $title,
            'body' => $body,
            'sender' => $usuario,
            'message' => $mensaje,
        ],
        'priority' => 'high',
    ];

    $ch = curl_init('https://fcm.googleapis.com/fcm/send');
    if ($ch === false) {
        respondJson(500, false, 'Could not initialize curl');
    }

    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Authorization: key=' . trim($serverKey),
        'Content-Type: application/json',
    ]);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 12);
    curl_setopt($ch, CURLOPT_TIMEOUT, 20);

    $responseBody = curl_exec($ch);
    $curlError = curl_error($ch);
    $httpCode = (int) curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($responseBody === false) {
        respondJson(502, false, $curlError === '' ? 'Could not send shout to FCM' : $curlError);
    }

    $fcmJson = json_decode($responseBody, true);
    if (!is_array($fcmJson)) {
        respondJson(502, false, 'Invalid response from FCM', ['fcm_response_raw' => $responseBody]);
    }

    if ($httpCode < 200 || $httpCode >= 300) {
        respondJson(502, false, 'FCM rejected the request', ['fcm_response' => $fcmJson]);
    }

    $successCount = isset($fcmJson['success']) ? (int)$fcmJson['success'] : 0;
    if ($successCount <= 0) {
        respondJson(502, false, 'FCM did not accept the message', ['fcm_response' => $fcmJson]);
    }

    respondJson(200, true, 'Shout sent', [
        'topic' => 'divas_global',
        'fcm_response' => $fcmJson,
    ]);
} catch (Throwable $throwable) {
    respondJson(500, false, 'Could not send shout');
}

