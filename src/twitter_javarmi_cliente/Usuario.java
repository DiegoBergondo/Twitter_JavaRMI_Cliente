package twitter_javarmi_cliente;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.List;

import twitter_javarmi_common.CallbackUsuarioInterface;
import twitter_javarmi_common.ServicioGestorInterface;
import twitter_javarmi_common.Gui;
import twitter_javarmi_common.ServicioAutenticacionInterface;
import twitter_javarmi_common.Trino;


public class Usuario {

	private static String miNick;
	private static ServicioAutenticacionInterface ServicioAuten;
	private static ServicioGestorInterface ServicioGestor;
	private static CallbackUsuarioInterface CallbackService;
	
	//Main, levanta el servicio y llama al menú principal.
	public static void main(String[] args) throws Exception {
		try{
			Registry RServer = LocateRegistry.getRegistry(8889);
			ServicioAuten = (ServicioAutenticacionInterface)RServer.lookup("Servidor");
			ServicioGestor = (ServicioGestorInterface)RServer.lookup("callback");
			CallbackService = new CallbackUsuarioImpl();

			gui();
		}
		
		catch (Exception excr) {
			System.out.println("No fue posible conectar con el servidor");
		}
	}
	
	//Menú principal con tres opciones posibles.
	private static void gui() throws RemoteException {

		int opt = 0;
		
		do {
			opt = Gui.menu("Menu Principal", 
					new String[]{ "Registrar un nuevo usuario", 
								  "Hacer login", 
								  "Salir" });
			
			//Según la opción escogida se llama a un método, a otro, o se sale.
			switch (opt) {
				case 0: registrarse(); break;
				case 1: autenticarse(); break;
			}
		}
		while (opt != 2);
		System.exit(0);

	}
	
	//Menú secundario, se accede al mismo tras logarse o registrarse-
	private static void menuSec() throws RemoteException {
		
		int opt = 0;
		do {
		opt = Gui.menu("Menu de Usuario", 
				new String[]{ "Información del Usuario", 
							  "Enviar Trino", 
							  "Listar Usuarios del Sistema",
							  "Seguir a",
							  "Dejar de seguir a",
							  "Salir Logout" });

			//Según la opción escogida se llama a un método, a otro, o se sale.
			switch (opt) {
				case 0: info(); break;
				case 1: enviarMensaje(); break;
				case 2: listarAmigos(); break;
				case 3: agregarContacto(); break;
				case 4: eliminarContacto(); break;
			}
		}
		while (opt != 5);
		//En caso de salir también se elimina al cliente del callback.
		ServicioGestor.unregisterForCallback(CallbackService);
	}
	
	//Muestra por pantalla la información de registro del usuario, el nombre de de los objetos remotos y el puerto.
	private static void info() throws RemoteException{
		System.out.println("Objetos remotos callback y Servidor en puerto 8889");
		System.out.println();
		System.out.println("Información del usuario");
		System.out.println();
		System.out.println("Mi nick es "+miNick);
		System.out.println("Mi nombre es "+ServicioAuten.getNombre(miNick));
		System.out.println("Mi email es "+ServicioAuten.getMail(miNick));
		System.out.println();
	}

	//Método de autenticación.
	private static void autenticarse() throws RemoteException {
		String nick = Gui.input("Autenticarse", "Ingrese su nick: ");
		String pass = Gui.input("Autenticarse", "Ingrese su password: ");
		if (ServicioAuten.autenticar(nick, pass)){
			miNick = nick;
			System.out.println("Te has autentificado correctamente");
			//Se llama a este método por si hay trinos pendientes en la base de datos, a partir de ahí ya se reciben del callback.
			recibirTrinos();
			CallbackService.setNick(miNick);
			ServicioGestor.registerForCallback(CallbackService);
			//Si todo ha ido bien se salta al menú secundario.
			menuSec();
		}
	else
		System.out.println("Ha habido un error en la autenticación, por favor, vuelve a intentarlo, si no estás registrado, hazlo mediante la opción 'Registrar un nuevo usuario'");					
	}

	//Método para registrar un usuario nuevo
	private static void registrarse() throws RemoteException{
		String nick = Gui.input("Registrarse", "Ingrese su nick: ");
		String pass = Gui.input("Registrarse", "Ingrese su password: ");
		String nombre = Gui.input("Registrarse", "Ingrese su nombre: ");
		String mail = Gui.input("Registrarse", "Ingrese su email: ");
		if(ServicioAuten.registrar(nick, pass, nombre, mail)){
			miNick = nick;
			CallbackService.setNick(miNick);
			ServicioGestor.registerForCallback(CallbackService);
			System.out.println("Tu usuario @"+miNick+" se ha registrado correctamente");
			//Si todo ha ido bien se salta al menú secundario.
			menuSec();
			}
		else
			System.out.println("El usuario ya está registrado, elige otro nick");
		}
	
	//Método para sacar por pantalla la lista de amigos del usuario
	private static void listarAmigos() throws RemoteException{
		List<String> amigos = ServicioGestor.amigos(miNick);
		if(amigos!=null  && !amigos.isEmpty()){
			for (String amigo : amigos) {
				System.out.println("@ " + amigo);
			}
		}
		else
			System.out.println("No tienes a ningún amigo en tu lista");
			System.out.println();
	}
	
	//Método para añadir a un amigo.
	private static void agregarContacto() throws RemoteException {
		String contacto = Gui.input("Agregar Contacto", "Ingrese el contacto: ");
		
		//Si el amigo ya está en la lista se informa por pantalla
		if (ServicioAuten.usuarioEnMiLista(contacto, miNick))
			System.out.println("El contacto ya está en tu lista");
		//Si no, se añade como amigo.
		else if (ServicioGestor.agregar(contacto, miNick))
			System.out.println("Se ha agregado el contacto " + contacto);
		//Y si el servidor no lo permite es que se quiere añadir a alguien que no está registrado.
		else
			System.out.println("El contacto no esta registrado");
	}
	
	//Método para eliminar a un amigo.
	private static void eliminarContacto() throws RemoteException {
		String contacto = Gui.input("Eliminar Contacto", "Ingrese el contacto: ");
		
		//Los condicionantes son iguales que en el método anterior.
		if (!ServicioAuten.usuarioEnMiLista(contacto, miNick))
			System.out.println("El contacto no está en tu lista");
		else if (ServicioGestor.eliminar(contacto, miNick))
			System.out.println("Se ha eliminado el contacto " + contacto);
		else
			System.out.println("El contacto no esta registrado");
	}
	
	//Método que saca por pantalla los trinos pendientes, sólo se usa en el proceso de login.	
	private static void recibirTrinos() throws RemoteException {
		List<Trino> trinos = ServicioGestor.recibir(miNick);
		
		if(trinos!=null){
			for (Trino trino : trinos) {
				System.out.println("@ " + trino.ObtenerNickPropietario());
				Date date = new Date(trino.ObtenerTimestamp());
				System.out.println("\t" + date + "    " + trino.ObtenerTrino() + "\n");
				}
			}
		System.out.println();
		}
	
	
	//Método que se usa para enviar un trino a un contecto
	private static void enviarMensaje() throws RemoteException {
		String opts[] = Gui.input("Enviar Mensaje", 
								  new String[]{ "Ingrese el nick del contacto: ",
												"Ingrese el mensaje: "});
		
		if(ServicioGestor.enviar(opts[1],miNick, opts[0]))
			ServicioGestor.nuevoTrino(opts[0]);
		//Si el servidor no deja enviar el trino es por que el usuario no está en la lista de amigos.
		else
			System.out.println("El usuario no esta en tu lista de amigos");
	}
}