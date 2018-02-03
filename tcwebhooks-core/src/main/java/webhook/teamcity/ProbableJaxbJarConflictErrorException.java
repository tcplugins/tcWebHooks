package webhook.teamcity;

import javax.xml.bind.JAXBException;

public class ProbableJaxbJarConflictErrorException extends Exception {

	private static final long serialVersionUID = -640816951469652309L;
	
	public ProbableJaxbJarConflictErrorException(JAXBException jaxbException) {
		super(jaxbException);
	}


}
