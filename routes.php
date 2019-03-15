<?php

use Slim\Http\Request;
use Slim\Http\Response;

// Routes

$app->get('/[{name}]', function (Request $request, Response $response, array $args) {
    // Sample log message
    $this->logger->info("Slim-Skeleton '/' route");

    // Render index view
    return $this->renderer->render($response, 'index.phtml', $args);
});

// Routes
// Grupo de rutas para el API
$app->group('/api', function () use ($app) {
  // Version group
  $app->group('/v1', function () use ($app) {
  	$app->get('/login/{nick}/{contrasena}', 'login');
    $app->get('/usuarios', 'obtenerUsuarios');
    $app->get('/usuario/{id}', 'obtenerUsuario');
    $app->get('/eventos', 'obtenerEventos');
    $app->get('/evento/{id}', 'obtenerEvento');
    $app->get('/eventoUsu/{nick}', 'obtenerEventoUsu');
    $app->post('/crearEvento', 'agregarEvento');
    $app->post('/crearUsuario', 'agregarUsuario');
    $app->post('/crearEstreno', 'agregarEstreno');
    $app->post('/crearParticipacion', 'agregarParticipacion');
    $app->put('/actualizarParticipacion/{id}', 'actualizarParticipacion');///////////
    $app->put('/actualizarUsu/{id}', 'actualizarUsuario');
    $app->put('/actualizarEvento/{id}', 'actualizarEvento');
    $app->delete('/eliminarUsu/{id}', 'eliminarUsuario');
    $app->delete('/eliminarEvento/{id}', 'eliminarEvento');
  });
});
