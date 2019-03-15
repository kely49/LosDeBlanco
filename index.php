<?php

if (PHP_SAPI == 'cli-server') {
    // To help the built-in PHP dev server, check if the request was actually for
    // something which should probably be served as a static file
    $url  = parse_url($_SERVER['REQUEST_URI']);
    $file = __DIR__ . $url['path'];
    if (is_file($file)) {
        return false;
    }
}

require __DIR__ . '/../vendor/autoload.php';

session_start();

// Instantiate the app
$settings = require __DIR__ . '/../src/settings.php';
$app = new \Slim\App($settings);

// Set up dependencies
require __DIR__ . '/../src/dependencies.php';

// Register middleware
require __DIR__ . '/../src/middleware.php';

// Register routes
require __DIR__ . '/../src/routes.php';

// Run app
$app->run();

function getConnection() {
    $dbhost="127.0.0.1";
    $dbuser="root";
    $dbpass="";
    $dbname="ldb_db";
    $dbh = new PDO("mysql:host=$dbhost;dbname=$dbname", $dbuser, $dbpass);
    $dbh->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    return $dbh;
}
function formatearDatos($nombreTabla,$array)
{
	return "{".$nombreTabla.":".json_encode($array)."}";
}

function login($request)
{
	$nick = $request->getAttribute('nick');
	$contrasena = $request->getAttribute('contrasena');
    $sql = "SELECT contrasena, admin FROM usuarios WHERE nick like '".$nick."'";
    try {
        $stmt = getConnection()->query($sql);
        $login = $stmt->fetchAll(PDO::FETCH_OBJ);
        $db = null;

        if(strcmp( $contrasena, $login[0] -> contrasena) == 0)
        {
        	//Login exitoso
        	if(($login[0] -> admin) == 1)
        	{
        		$token = -10;
        	}
        	else{
        		$token = rand(0, 1000000);
        	}
        }
        else
        {
        	$token = -1;
        }
        //devuelve token
        return "".$token;
    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}
//FUNCIONA
function obtenerUsuarios($response) {

	$nombreTabla = "usuarios";

    $sql = "SELECT nick, nombreUsu, apellidoUsu, fechaNac FROM usuarios";
    try {
        $stmt = getConnection()->query($sql);
        $arrayUsuarios = $stmt->fetchAll(PDO::FETCH_OBJ);
        $db = null;

        return formatearDatos($nombreTabla,$arrayUsuarios);
    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}

//FUNCIONA
function obtenerUsuario($request) {
    $idUsu = $request->getAttribute('id');
    $sql = "SELECT nick, nombreUsu, apellidoUsu, fechaNac FROM usuarios WHERE idUsu=".$idUsu;
    try {
        $stmt = getConnection()->query($sql);
        $usuarios = $stmt->fetchAll(PDO::FETCH_OBJ);
        $db = null;
        
        return json_encode($usuarios);
    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}

//FUNCIONA
function obtenerEventos($response) {

	$nombreTabla = "eventos";

    $sql = "SELECT nombreEvento, fechaEvento, foto FROM eventos";
    try {
        $stmt = getConnection()->query($sql);
        $arrayEventos = $stmt->fetchAll(PDO::FETCH_OBJ);
        $db = null;

        return formatearDatos($nombreTabla,$arrayEventos);
    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}

//FUNCIONA
function obtenerEvento($request) {
    $idEvento = $request->getAttribute('id');
    $sql = "SELECT nombreEvento, fechaEvento, foto FROM eventos WHERE idEvento=".$idEvento;
    try {
        $stmt = getConnection()->query($sql);
        $eventos = $stmt->fetchAll(PDO::FETCH_OBJ);
        $db = null;
        
        return json_encode($eventos);
    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}

//FUNCIONA
function obtenerEventoUsu($request) {

    $nick = $request->getAttribute('nick');
    $sql = "SELECT nombreEvento, idUsu, idEquipo FROM eventos NATURAL JOIN participacion NATURAL JOIN usuarios NATURAL JOIN miembros WHERE nick like '".$nick."'";

    try {
        $stmt = getConnection()->query($sql);
        $eventos = $stmt->fetchAll(PDO::FETCH_OBJ);
        $db = null;
        //Guardamos el id del usuario que nos pasan y el numero de eventos
        $idUsu = $eventos[0] -> idUsu;
        $idEquipo = $eventos[0] -> idEquipo;

        $numEventos = count($eventos);
        $numCompañeros = array();
        //array en el que se van almacenando todos los datos a mostrar
        $muestraFinal = array($numEventos);

	//Recorrer array $eventos, sacar usuarios de cada evento
	for($i = 0; $i < count($eventos); $i++)
	{
		
		$sql = "SELECT idEvento, idEquipo FROM participacion NATURAL JOIN eventos WHERE nombreEvento like '".$eventos[$i] -> nombreEvento."' AND idUsu =".$idUsu;
		$stmt2 = getConnection()->query($sql);
        $equipos = $stmt2->fetchAll(PDO::FETCH_OBJ);
        $db = null;
        $idEquipo = $equipos[0] -> idEquipo;
        $idEvento = $equipos[0] -> idEvento;

       	//Para cada evento sacamos el numero de compañeros
        for($j = 0; $j < count($equipos);$j++)
        {
        	$sql = "SELECT idUsu, superviviente FROM participacion WHERE idEquipo = ".$idEquipo." AND idEvento = ".$idEvento;
        	$stmt3 = getConnection()->query($sql);
        	$miembros = $stmt3->fetchAll(PDO::FETCH_OBJ);
        	$db = null;
        	//creamos 2 variables para calcular el porcentaje de compañeros supervivientes en un evento
        	$numSupervivientes = 0;
        	$numInfectados = 0;
        	//comprobamos si cada uno de los compañeros ha sobrevivido o no
        	if(($miembros[$j]->superviviente) == 0)
        	{
        		$estado = "infectado";
        		$numInfectados++;
        	}
        	else{
        		$estado = "superviviente";
        		$numSupervivientes++;
        	}
        	//Añadimos el numero de compañeros de cada edicion al array
        	array_push($numCompañeros,count($miembros));
        }
        $porcentajeInfectado = ($numInfectados*100)/count($numCompañeros);

        array_push($muestraFinal, count($numCompañeros)-1, $estado, $porcentajeInfectado ,$eventos[$i]->nombreEvento, $equipos);
		//Crear array de struct
	}
	//Queda procentaje del grupo infectado
        return json_encode($muestraFinal);
        //el JSON devuelve con los datos de la siguiente manera:
        //[         2,                      0,              "infectado",             100    "SZ Aviles",[{"idEvento":"1","idEquipo":"1"}]
        //numero total de eventos, numero de compañeros,  Ha sobrevivido o no, porcentaje infectado, nombre del evento, id del evento, id del equipo
        //El numero total de eventos, solo es el numero al inicio del JSON

    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}

//FUNCIONA CON POSTMAN
function agregarUsuario($request) {
    $emp = json_decode($request->getBody());
    
    $sql = "INSERT INTO usuarios (nick, contrasena, nombreUsu, apellidoUsu, fechaNac) VALUES (:nick, :contrasena, :nombreUsu, :apellidoUsu, :fechaNac)";
    try {
        $db = getConnection();
        $stmt = $db->prepare($sql);
        $stmt->bindParam("nick", $emp->nick);
        $stmt->bindParam("contrasena", $emp->contrasena);
        $stmt->bindParam("nombreUsu", $emp->nombreUsu);
		$stmt->bindParam("apellidoUsu", $emp->apellidoUsu);
		$stmt->bindParam("fechaNac", $emp->fechaNac);
        $stmt->execute();
        $emp->id = $db->lastInsertId();
        $db = null;
        echo json_encode($emp);
    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}

//FUNCIONA CON POSTMAN
function agregarEstreno($request) {
    $emp = json_decode($request->getBody());
    
    $sql = "INSERT INTO estrenos (nombre, descripcion, fecha, URL) VALUES (:nombre, :descripcion, :fecha, :URL)";
    try {
        $db = getConnection();
        $stmt = $db->prepare($sql);
        $stmt->bindParam("nombre", $emp->nombre);
        $stmt->bindParam("descripcion", $emp->descripcion);
        $stmt->bindParam("fecha", $emp->fecha);
        $stmt->bindParam("URL", $emp->URL);
        $stmt->execute();
        $emp->id = $db->lastInsertId();
        $db = null;
        echo json_encode($emp);
    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}

//FUNCIONA CON POSTMAN
function agregarParticipacion($request) {
    $part = json_decode($request->getBody());
    
    $sql = "INSERT INTO participacion (pruebasRealizadas, superviviente, idUsu, idEvento) VALUES (:pruebasRealizadas, :superviviente, :idUsu, :idEvento)";
    try {
        $db = getConnection();
        $stmt = $db->prepare($sql);
        $stmt->bindParam("pruebasRealizadas", $part->pruebasRealizadas);
        $stmt->bindParam("superviviente", $part->superviviente);
        $stmt->bindParam("idUsu", $part->idUsu);
        $stmt->bindParam("idEvento", $part->idEvento);
        $stmt->execute();
        $emp->id = $db->lastInsertId();
        $db = null;
        echo json_encode($part);
    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}

//FUNCIONA CON POSTMAN
function agregarEvento($request) {
    $emp = json_decode($request->getBody());
    
    $sql = "INSERT INTO eventos (nombreEvento, fechaEvento) VALUES (:nombreEvento, :fechaEvento)";
    try {
        $db = getConnection();
        $stmt = $db->prepare($sql);
        $stmt->bindParam("nombreEvento", $emp->nombreEvento);
        $stmt->bindParam("fechaEvento", $emp->fechaEvento);
        $stmt->execute();
        $emp->id = $db->lastInsertId();
        $db = null;
        echo json_encode($emp);
    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}

//FUNCIONA CON POSTMAN
function actualizarUsuario($request) {
    $usu = json_decode($request->getBody());
    $id = $request->getAttribute('id');
    $sql = "UPDATE usuarios SET nick=:nick, contrasena=:contrasena, nombreUsu=:nombreUsu, apellidoUsu=:apellidoUsu, fechaNac=:fechaNac WHERE idUsu=:id";
    try {
        $db = getConnection();
        $stmt = $db->prepare($sql);
        $stmt->bindParam("nick", $usu->nick);
        $stmt->bindParam("contrasena", $usu->contrasena);
        $stmt->bindParam("nombreUsu", $usu->nombreUsu);
		$stmt->bindParam("apellidoUsu", $usu->apellidoUsu);
		$stmt->bindParam("fechaNac", $usu->fechaNac);
        $stmt->bindParam("id", $id);
        $stmt->execute();
        $db = null;
        echo json_encode($usu);

    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}

//FUNCIONA CON POSTMAN
function actualizarParticipacion($request) {
    $part = json_decode($request->getBody());
    $id = $request->getAttribute('id');
    $sql = "UPDATE participacion SET idEquipo=:idEquipo WHERE idParticipacion=:id";
    try {
        $db = getConnection();
        $stmt = $db->prepare($sql);
        $stmt->bindParam("idEquipo", $part->idEquipo);
        $stmt->bindParam("id", $id);
        $stmt->execute();
        $db = null;
        echo json_encode($part);

    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}

//FUNCIONA CON POSTMAN
function actualizarEvento($request) {
    $evento = json_decode($request->getBody());
    $id = $request->getAttribute('id');
    $sql = "UPDATE eventos SET nombreEvento=:nombreEvento, fechaEvento=:fechaEvento WHERE idEvento=:id";
    try {
        $db = getConnection();
        $stmt = $db->prepare($sql);
        $stmt->bindParam("nombreEvento", $evento->nombreEvento);
        $stmt->bindParam("fechaEvento", $evento->fechaEvento);
        $stmt->bindParam("id", $id);
        $stmt->execute();
        $db = null;
        echo json_encode($evento);

    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}

//FUNCIONA CON POSTMAN
function eliminarUsuario($request) {
    $id = $request->getAttribute('id');
    $sql = "DELETE FROM usuarios WHERE idUsu=:id";
    try {
        $db = getConnection();
        $stmt = $db->prepare($sql);
        $stmt->bindParam("id", $id);
        $stmt->execute();
        $db = null;
        echo "se elimino el usuario";
    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}

//FUNCIONA CON POSTMAN
function eliminarEvento($request) {
    $id = $request->getAttribute('id');
    $sql = "DELETE FROM eventos WHERE idevento=:id";
    try {
        $db = getConnection();
        $stmt = $db->prepare($sql);
        $stmt->bindParam("id", $id);
        $stmt->execute();
        $db = null;
        echo "se elimino el evento";
    } catch(PDOException $e) {
        echo '{"error":{"text":'. $e->getMessage() .'}}';
    }
}