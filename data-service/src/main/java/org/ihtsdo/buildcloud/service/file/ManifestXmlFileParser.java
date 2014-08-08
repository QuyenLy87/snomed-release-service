package org.ihtsdo.buildcloud.service.file;

import static org.ihtsdo.buildcloud.service.execution.RF2Constants.MANIFEST_CONTEXT_PATH;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.ihtsdo.buildcloud.manifest.ListingType;
import org.ihtsdo.buildcloud.service.exception.ResourceNotFoundException;

public class ManifestXmlFileParser {
    
    /**
     * @param manifestInputSteam The InputStream of a manifest XML file.
     * @return A ListingType
     * @throws JAXBException
     * @throws ResourceNotFoundException
     */
    public final ListingType parse(final InputStream manifestInputSteam) throws JAXBException, ResourceNotFoundException {
	//Get the manifest file as an input stream
	if (manifestInputSteam == null) {
		throw new ResourceNotFoundException("Failed to load manifest due to null inputstream");
	}
	//Load the manifest file xml into a java object hierarchy
	JAXBContext jc = JAXBContext.newInstance(MANIFEST_CONTEXT_PATH);
	Unmarshaller um = jc.createUnmarshaller();
	ListingType manifestListing = um.unmarshal(new StreamSource(manifestInputSteam), ListingType.class).getValue();

	if (manifestListing.getFolder() == null) {
	    throw new ResourceNotFoundException("Failed to recover root folder from manifest.  Ensure the root element is named 'listing' "
		    + "and it has a namespace of xmlns=\"http://release.ihtsdo.org/manifest/1.0.0\" ");
	}
	return manifestListing;
}

}
