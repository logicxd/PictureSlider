public class InvalidFolderException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidFolderException() {
		super("Something went wrong opening the folder. Make sure the folder contains only pictures.");
	}
}
