
public class InvalidFileException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidFileException() {
		super("Something went wrong opening the files. Make sure the files are all pictures.");
	}
}
