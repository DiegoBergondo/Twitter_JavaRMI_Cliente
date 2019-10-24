package twitter_javarmi_cliente;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import twitter_javarmi_common.CallbackUsuarioInterface;

public class CallbackUsuarioImpl extends UnicastRemoteObject implements CallbackUsuarioInterface {  
	
	private static final long serialVersionUID = 1L;
	String Nick;
	
	public CallbackUsuarioImpl() throws RemoteException {
		super( );
		}
	
	//Método que devuelve el trino ya en su formato final
	public String notificarTrino(String trino){
		String returnTrino = "# " + trino;
		System.out.println(returnTrino);
		return returnTrino;
		}    
	
	//Método para asignar nick al callback.
	public void setNick(String nick){
		Nick = nick;
		}
	
	//Método que devuelve el nick del callback.
	public String getNick(){
		return Nick;
	}
	
}
