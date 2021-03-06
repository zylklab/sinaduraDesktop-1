/*
 * # Copyright 2008 zylk.net 
 * # 
 * # This file is part of Sinadura. 
 * # 
 * # Sinadura is free software: you can redistribute it and/or modify 
 * # it under the terms of the GNU General Public License as published by 
 * # the Free Software Foundation, either version 2 of the License, or 
 * # (at your option) any later version. 
 * # 
 * # Sinadura is distributed in the hope that it will be useful, 
 * # but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * # GNU General Public License for more details. 
 * # 
 * # You should have received a copy of the GNU General Public License 
 * # along with Sinadura. If not, see <http://www.gnu.org/licenses/>. [^] 
 * # 
 * # See COPYRIGHT.txt for copyright notices and details. 
 * #
 */
/*
 * # Copyright 2008 zylk.net 
 * # 
 * # This file is part of Sinadura. 
 * # 
 * # Sinadura is free software: you can redistribute it and/or modify 
 * # it under the terms of the GNU General Public License as published by 
 * # the Free Software Foundation, either version 2 of the License, or 
 * # (at your option) any later version. 
 * # 
 * # Sinadura is distributed in the hope that it will be useful, 
 * # but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * # GNU General Public License for more details. 
 * # 
 * # You should have received a copy of the GNU General Public License 
 * # along with Sinadura. If not, see <http://www.gnu.org/licenses/>. [^] 
 * # 
 * # See COPYRIGHT.txt for copyright notices and details. 
 * #
 */
package net.esle.sinadura.gui.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.exceptions.BadPasswordException;

import es.mityc.firmaJava.libreria.utilidades.URIEncoder;
import net.esle.sinadura.core.certificate.CertificateUtil;
import net.esle.sinadura.core.exceptions.ConnectionException;
import net.esle.sinadura.core.exceptions.CoreException;
import net.esle.sinadura.core.exceptions.CorePKCS12Exception;
import net.esle.sinadura.core.exceptions.NoSunPkcs11ProviderException;
import net.esle.sinadura.core.exceptions.OCSPCoreException;
import net.esle.sinadura.core.exceptions.OCSPIssuerRequiredException;
import net.esle.sinadura.core.exceptions.OCSPUnknownUrlException;
import net.esle.sinadura.core.exceptions.PKCS11Exception;
import net.esle.sinadura.core.exceptions.PasswordCallbackCanceledException;
import net.esle.sinadura.core.exceptions.PdfSignatureException;
import net.esle.sinadura.core.exceptions.RevokedException;
import net.esle.sinadura.core.exceptions.XadesSignatureException;
import net.esle.sinadura.core.keystore.KeyStoreBuilderFactory;
import net.esle.sinadura.core.keystore.KeyStoreBuilderFactory.KeyStoreTypes;
import net.esle.sinadura.core.keystore.PKCS11Helper;
import net.esle.sinadura.core.model.KsSignaturePreferences;
import net.esle.sinadura.core.model.PdfSignatureField;
import net.esle.sinadura.core.model.PdfSignaturePreferences;
import net.esle.sinadura.core.model.XadesSignaturePreferences;
import net.esle.sinadura.core.password.DummyCallbackHandler;
import net.esle.sinadura.core.password.PasswordExtractor;
import net.esle.sinadura.core.service.PdfService;
import net.esle.sinadura.core.service.XadesService;
import net.esle.sinadura.core.util.FileUtil;
import net.esle.sinadura.core.util.PdfUtil;
import net.esle.sinadura.gui.events.ProgressWriter;
import net.esle.sinadura.gui.exceptions.AliasesNotFoundException;
import net.esle.sinadura.gui.exceptions.DriversNotFoundException;
import net.esle.sinadura.gui.exceptions.OverwritingException;
import net.esle.sinadura.gui.model.DocumentInfo;
import net.esle.sinadura.gui.util.LanguageUtil;
import net.esle.sinadura.gui.util.PdfProfile;
import net.esle.sinadura.gui.util.PreferencesUtil;
import net.esle.sinadura.gui.util.StatisticsUtil;

public class SignController {

	private static final Log log = LogFactory.getLog(SignController.class);
	

	public static Map<String, Long> loadSlot(String certificadoType, String certificadoPath) throws NoSuchAlgorithmException, KeyStoreException, PKCS11Exception,
			CoreException, CorePKCS12Exception, DriversNotFoundException {

		// compruebo las preferencias y cargo el certificado del dispositivo
		// hardware, o del el file-system
		Map<String, Long> slotsByReader = new HashMap<String, Long>();
		
		if (certificadoType.equalsIgnoreCase(PreferencesUtil.CERT_TYPE_VALUE_HARDWARE)) 
		{
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
			
				PKCS11Helper pk11h = new PKCS11Helper(certificadoPath, "");
				// TODO revisar si es necesario
				// long[] slots = null;
				// slots = pk11h.getSignatureCapableSlots();
				slotsByReader = pk11h.getSoltsByReaderName();
			}

		} else if (certificadoType.equalsIgnoreCase(PreferencesUtil.CERT_TYPE_VALUE_SOFTWARE)) {

			//do nothing
		}
		else if (certificadoType.equalsIgnoreCase(PreferencesUtil.CERT_TYPE_VALUE_MSCAPI)) {

			//do nothing

		}

		return slotsByReader;
	}

	public static KsSignaturePreferences loadKeyStore(Shell sShell, String certificadoType, String certificadoPath, String slot) throws NoSuchAlgorithmException, KeyStoreException, PKCS11Exception, NoSunPkcs11ProviderException, 
			CoreException, CorePKCS12Exception {

		PasswordCallbackHandlerDialog o = new PasswordCallbackHandlerDialog(sShell);
		PasswordExtractor pe = (PasswordExtractor) o;

		KsSignaturePreferences ksSignaturePreferences = new KsSignaturePreferences();
		KeyStore ks = null;

		// compruebo las preferencias y cargo el certificado del dispositivo
		// hardware, o del file-system
		if (certificadoType.equalsIgnoreCase(PreferencesUtil.CERT_TYPE_VALUE_HARDWARE)) {

			StatisticsUtil.log(StatisticsUtil.KEY_SIGN_CERTTYPE, StatisticsUtil.VALUE_HARD);
			log.info("sign type: " + StatisticsUtil.VALUE_HARD);
			
			StatisticsUtil.log(StatisticsUtil.KEY_LOAD_HARDWAREDRIVER, certificadoPath);
			log.info("sign driver: " + certificadoPath);

			ks = KeyStoreBuilderFactory.getKeyStore("HARD", KeyStoreTypes.PKCS11, certificadoPath, slot, new KeyStore.CallbackHandlerProtection(
					o));

		} else if (certificadoType.equalsIgnoreCase(PreferencesUtil.CERT_TYPE_VALUE_SOFTWARE)) {

			StatisticsUtil.log(StatisticsUtil.KEY_SIGN_CERTTYPE, StatisticsUtil.VALUE_SOFT);
			ks = KeyStoreBuilderFactory.getKeyStore("SOFT", KeyStoreTypes.PKCS12, certificadoPath, new KeyStore.CallbackHandlerProtection(o));
			
		} else if (certificadoType.equalsIgnoreCase(PreferencesUtil.CERT_TYPE_VALUE_MSCAPI)) {

			StatisticsUtil.log(StatisticsUtil.KEY_SIGN_CERTTYPE, StatisticsUtil.VALUE_MSCAPI);
			DummyCallbackHandler a = new DummyCallbackHandler(null);
			pe = (PasswordExtractor) a;
			ks = KeyStoreBuilderFactory.getKeyStore("MSCAPI", KeyStoreTypes.MSCAPI, null, new KeyStore.CallbackHandlerProtection(a));

		}

		ksSignaturePreferences.setKs(ks);

		// fijo el passwordprotection para el PKCS12, para el PKCS11 no es
		// necesario pero por coherencia lo uso tambien.
		ksSignaturePreferences.setPasswordProtection(pe.getPasswordProtection());

		return ksSignaturePreferences;
	}

	public static void logout(KeyStore ks, String alias) throws NoSunPkcs11ProviderException {

		// una vez que se ha firmado... hago un logout de la session del
		// provider
		KeyStoreBuilderFactory.logout(ks, alias);
	}

	/************************************************************
	 * Se obtien el alias del certificado con el que firmar, 
	 * pero siguiendo unas preferencias (si está el check habilitado)
	 * #13187
	 *************************************************************/
	public static List<String> getAlias(KeyStore ks) throws AliasesNotFoundException, KeyStoreException {

		// clasificamos los certificados en base a la selección de preferencias
		//--------------
		boolean aplicarPreferencias = PreferencesUtil.getBoolean(PreferencesUtil.APLICAR_PREFERENCIAS_USAGE_CERT);
		
		List<String> certificadosNonRepudiation = new ArrayList<String>();
		List<String> certificadosDigitalSignature = new ArrayList<String>();
		List<String> certificadosSinKeyUsage = new ArrayList<String>();
		List<String> certificadosTodos = new ArrayList<String>();
		
		String certAlias;
		X509Certificate certificado;
		Enumeration<String> aliases = ks.aliases();
		
		StatisticsUtil.log(StatisticsUtil.KEY_CERTIFICADO_NUMERO, String.valueOf(ks.size()));
		log.info("Estadisticas | " + StatisticsUtil.KEY_CERTIFICADO_NUMERO + ": " + String.valueOf(ks.size()));
		
		while (aliases.hasMoreElements()) {

			certAlias = aliases.nextElement();
			certificado = (X509Certificate)ks.getCertificate(certAlias);
			
			StatisticsUtil.log(StatisticsUtil.KEY_CERTIFICADO_USAGE, CertificateUtil.getKeyUsage(certificado));
			log.info("Estadisticas | " + StatisticsUtil.KEY_CERTIFICADO_USAGE + ": " + CertificateUtil.getKeyUsage(certificado));

			
			if (aplicarPreferencias){
				if (CertificateUtil.esNonRepudiation(certificado)){
					certificadosNonRepudiation.add(certAlias);	
				}else if (CertificateUtil.esDigitalSignature(certificado)){
					certificadosDigitalSignature.add(certAlias);
				}else if (CertificateUtil.keyUsageNoDefinido(certificado)){
					certificadosSinKeyUsage.add(certAlias);
				}	
			}else{
				certificadosTodos.add(certAlias);				
			}
		}
		
		// obtenemos los certificados
		//--------------
		if (aplicarPreferencias){
			if (certificadosNonRepudiation.size() == 0 && 
				certificadosDigitalSignature.size() == 0 &&
				certificadosSinKeyUsage.size() == 0) {
				throw new AliasesNotFoundException();
			}else{
				if (certificadosNonRepudiation.size() > 0){
					return certificadosNonRepudiation;				
				}else if (certificadosDigitalSignature.size() > 0){
					return certificadosDigitalSignature;
				}else{
					return certificadosSinKeyUsage;
				}
			}
		}else{
			if (certificadosTodos.size() == 0) {
				throw new AliasesNotFoundException();
			}else{
				return certificadosTodos;
			}
		}
	}

	public static void sign(DocumentInfo pdfParameter, KsSignaturePreferences ksSignaturePreferences, PdfProfile pdfProfile, Shell sShell) throws OCSPCoreException,
			RevokedException, ConnectionException, CertificateExpiredException, CertificateNotYetValidException,
			OCSPIssuerRequiredException, OCSPUnknownUrlException, InterruptedException {

		try {
			StatisticsUtil.log(StatisticsUtil.KEY_SIGN_DOCUMENT_EXTENSION, FileUtil.getExtension(pdfParameter.getPath()));
			log.info("extension del documento: " + FileUtil.getExtension(FileUtil.getLocalPathFromURI(pdfParameter.getPath())));
			
			StatisticsUtil.log(StatisticsUtil.KEY_SIGN_MIMETYPE, pdfParameter.getMimeType());
			log.info("mimetype del documento: " + pdfParameter.getMimeType());
			
			StatisticsUtil.log(StatisticsUtil.KEY_SIGN_DOCUMENT_SIZE, new File(pdfParameter.getPath()).length() + "");
			log.info("size del documento: " + new File(pdfParameter.getPath()).length() + "");

			StatisticsUtil.log(StatisticsUtil.KEY_SIGN_TSA, PreferencesUtil.getBoolean(PreferencesUtil.SIGN_TS_ENABLE)
					+ "");
			log.info("tsa enable: " + PreferencesUtil.getBoolean(PreferencesUtil.SIGN_TS_ENABLE)
					+ "");

			// firma
			if (pdfParameter.getMimeType() != null && pdfParameter.getMimeType().equals(FileUtil.MIMETYPE_PDF)) {
				
				if (PreferencesUtil.getString(PreferencesUtil.PDF_TIPO).equals(PreferencesUtil.PDF_TIPO_XML)) {
					signXades(pdfParameter, ksSignaturePreferences);
					
				}else{
					boolean successfullySigned = false;
					PasswordProtection ownerPassword = null;
					
					while (!successfullySigned) {
						try {
							signPDF(pdfParameter, ksSignaturePreferences, pdfProfile, ownerPassword, sShell);
							successfullySigned = true;
							
						} catch (BadPasswordException e) {

							PasswordDialogRunnable runnable = new PasswordDialogRunnable(null, LanguageUtil.getLanguage().getString(
									"password.dialog.passwordprotected"));
							
							Display.getDefault().syncExec(runnable);
							if (runnable.getPasswordProtection() == null) {
								throw new PasswordCallbackCanceledException();
							}
							ownerPassword = runnable.getPasswordProtection();
						}
					}					
				}
					
			} else if (pdfParameter.getMimeType() != null && pdfParameter.getMimeType().equals(FileUtil.MIMETYPE_XML)) {
				// TODO Aqui tendria sentido hacer una firma enveloped
				signXades(pdfParameter, ksSignaturePreferences);
				
			} else if (pdfParameter.getMimeType() != null && pdfParameter.getMimeType().equals(FileUtil.MIMETYPE_SAR)) {
				signXades(pdfParameter, ksSignaturePreferences);
				
			} else { // un documento cualquiera
				signXades(pdfParameter, ksSignaturePreferences);
			}
			
		} catch (PasswordCallbackCanceledException e) {
			
			String m = MessageFormat.format(LanguageUtil.getLanguage().getString(
				"error.document.notsigned.passwordlocked"), FileUtil.getLocalPathFromURI(pdfParameter.getPath()));
			log.error(m, e);
			Display.getDefault().syncExec(new ProgressWriter(ProgressWriter.ERROR, m));
			
		} catch (OverwritingException e) {

			File file = FileUtil.getLocalFileFromURI(pdfParameter.getPath());
			String fileDestino = PreferencesUtil.getOutputDir(file) + File.separatorChar + PreferencesUtil.getOutputName(file.getName());

			String m = MessageFormat.format(LanguageUtil.getLanguage().getString("error.overwrite"), FileUtil.getLocalFileFromURI(pdfParameter.getPath()), FileUtil.getLocalPathFromURI(fileDestino));

			log.error(m, e);
			Display.getDefault().syncExec(new ProgressWriter(ProgressWriter.ERROR, m));

		} catch (IOException e) {

			String m = MessageFormat.format(LanguageUtil.getLanguage().getString("error.certificate.sign.unexpected"),
					FileUtil.getLocalPathFromURI(pdfParameter.getPath()), e.toString());
			log.error(m, e);
			Display.getDefault().syncExec(new ProgressWriter(ProgressWriter.ERROR, m));
		}
	}
	

	private static void signPDF(DocumentInfo pdfParameter, KsSignaturePreferences ksSignaturePreferences, PdfProfile pdfProfile, PasswordProtection ownerPassword, Shell sShell) throws OCSPCoreException,
			RevokedException, OverwritingException, ConnectionException, IOException, CertificateExpiredException,
			CertificateNotYetValidException, OCSPIssuerRequiredException, OCSPUnknownUrlException, InterruptedException {
		
		try {
			
			Map<String, PdfProfile> availableProfiles = PreferencesUtil.getPdfProfiles();
			
			// map por acrofield para simplificar la logica --> preferencias
			Map<String, PdfProfile> availableProfilesMap = new HashMap<String, PdfProfile>();
			for (PdfProfile p : availableProfiles.values()) {
				availableProfilesMap.put(p.getAcroField(), p);
			}
			
			// 1- seleccion del hueco
			String signatureName = null;
			
			List<PdfSignatureField> blankSignatureNames = PdfUtil.getBlankSignatureFields(pdfParameter.getPath(), ownerPassword);
			
			if (blankSignatureNames != null && blankSignatureNames.size() == 1) { // 1 hueco de firma
				
				signatureName = blankSignatureNames.get(0).getName();
				
			} else if (blankSignatureNames != null && blankSignatureNames.size() > 1) { // N huecos de firma
				
				int resolutionsCount = 0;
				for (PdfSignatureField blankSignatureName : blankSignatureNames) {
					
					PdfProfile resolutionPdfProfile = availableProfilesMap.get(blankSignatureName.getName());
					if (resolutionPdfProfile != null) {
						resolutionsCount++;
						signatureName = blankSignatureName.getName();
					}
					
				}
				
				if (resolutionsCount == 0 || resolutionsCount > 1) {
					// si hay mas de un hueco de firma y no se resuelve ninguno o bien se resuelven mas de uno, hay que preguntar al usuario.
					
					// TODO En la ventana de seleccion del hueco ahora se muestra siempre la imagen del perfil por defecto.
					// Tendria mas sentido mostrar en cada hueco la imagen del perfil asociado. Si un hueco no tiene perfil
					// asociando sí hay que utilizar la del perfil por defecto.
					// De todas formas esto solo seria para cuando haya 2 o mas huecos con un perfil asociado, asi que es un caso que
					// apenas se va a producir. 
					// 
					// Ahora en la clase "PdfSignatureFieldSelectorDialog" "stampPath" es un unico parametro de entrada. Habria que
					// implementar esta logica dentro de esa clase ya que es donde se alterna entre los distintos huecos.  
					String stampPath = null;
					if (resolutionsCount == 0) {
						if (pdfProfile.hasImage()) {
							stampPath = pdfProfile.getImagePath();
						}
					}

					SignatureFieldSelectorRunnable sfsr = new SignatureFieldSelectorRunnable(sShell, pdfParameter, ownerPassword, blankSignatureNames, stampPath);
					Display.getDefault().syncExec(sfsr);
					
					if (sfsr.getSelectedSignatureField() != null) {
						signatureName = sfsr.getSelectedSignatureField().getName();	
					} else {
						// interrumped exception (cancelar)
						throw new InterruptedException();
					}
				}
				
			}

			// 2- re-seleccion del profile en base al acrofieldName (hueco)
			if (signatureName != null) {
				PdfProfile resolutionPdfProfile = availableProfilesMap.get(signatureName);
				if (resolutionPdfProfile != null) {
					pdfProfile = resolutionPdfProfile;
				}
			}
			
			PdfSignatureField pdfBlankSignatureInfo = new PdfSignatureField();
			pdfBlankSignatureInfo.setPage(pdfProfile.getPage());
			pdfBlankSignatureInfo.setStartX(pdfProfile.getStartX());
			pdfBlankSignatureInfo.setStartY(pdfProfile.getStartY());
			pdfBlankSignatureInfo.setWidht(pdfProfile.getWidht());
			pdfBlankSignatureInfo.setHeight(pdfProfile.getHeight());
			
			// ASK POSITION
			if (pdfProfile.getVisible()) {
			
				if (pdfProfile.getAskPosition()) {

					String stampPath = null;
					if (pdfProfile.hasImage()) {
						stampPath = pdfProfile.getImagePath();
					}
					
					SignatureFieldPositionRunnable sfpr = new SignatureFieldPositionRunnable(sShell, pdfBlankSignatureInfo, pdfParameter, ownerPassword, stampPath);
					Display.getDefault().syncExec(sfpr);
					
					if (sfpr.getSignatureField() != null) {
						pdfBlankSignatureInfo = sfpr.getSignatureField();
					} else {
						// interrumped exception (cancelar)
						throw new InterruptedException();
					}	
				}
			}

			PdfSignaturePreferences signaturePreferences = new PdfSignaturePreferences();

			signaturePreferences.setAcroField(signatureName);
			
			signaturePreferences.setPage(pdfBlankSignatureInfo.getPage());
			signaturePreferences.setStartX(pdfBlankSignatureInfo.getStartX());
			signaturePreferences.setStartY(pdfBlankSignatureInfo.getStartY());
			signaturePreferences.setWidht(pdfBlankSignatureInfo.getWidht());
			signaturePreferences.setHeight(pdfBlankSignatureInfo.getHeight());
			
			signaturePreferences.setReason(pdfProfile.getReason());
			signaturePreferences.setLocation(pdfProfile.getLocation());
			signaturePreferences.setVisible(pdfProfile.getVisible());
			
			Image sello = null;
			if (pdfProfile.hasImage()) {
				try {
					sello = Image.getInstance(pdfProfile.getImagePath());

				} catch (BadElementException e) {
					log.error("", e);

				} catch (MalformedURLException e) {
					log.error("", e);

				} catch (IOException e) {
					log.error("", e);
				}
			}
			signaturePreferences.setImage(sello);

			signaturePreferences.setCertified(pdfProfile.getCertified());

			
			// ks
			signaturePreferences.setKsSignaturePreferences(ksSignaturePreferences);
			
			signaturePreferences.setKsCache(PreferencesUtil.getCacheKeystoreComplete());

			/////////////////////////////
			
			String tsurl = null;
			String tsaOcspUrl = null; // en la firma de PDF este dato no es necesario, pero lo añado igualmente.
			if (PreferencesUtil.getBoolean(PreferencesUtil.SIGN_TS_ENABLE) == true) {
				tsurl = PreferencesUtil.getTimestampPreferences().get(PreferencesUtil.getString(PreferencesUtil.SIGN_TS_TSA)).getUrl();
				tsaOcspUrl = PreferencesUtil.getTimestampPreferences().get(PreferencesUtil.getString(PreferencesUtil.SIGN_TS_TSA)).getOcspUrl();
			}
			signaturePreferences.setTimestampUrl(tsurl);
			signaturePreferences.setTimestampOcspUrl(tsaOcspUrl);
			signaturePreferences.setTimestampUser(null);
			signaturePreferences.setTimestampPassword(null);

			boolean addOCSP = PreferencesUtil.getBoolean(PreferencesUtil.SIGN_OCSP_ENABLE);
			signaturePreferences.setAddOCSP(addOCSP);

			StatisticsUtil.log(StatisticsUtil.KEY_SIGN_OCSP, addOCSP + "");
			log.info("ocsp enable: " + addOCSP);

			
			InputStream is;
			try {
				is = FileUtil.getInputStreamFromURI(pdfParameter.getPath());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				log.error("error creando el InputStream asociado al fichero de entrada " + FileUtil.getLocalPathFromURI(pdfParameter.getPath()) , e);
				throw new IOException(e);
			}
			
			
			URI fileUri;
			String outputPath = null;
			try {
				fileUri = new URI(FileUtil.urlEncoder(pdfParameter.getPath()));
			} catch (URISyntaxException e1) {
				log.error(e1);
				throw new IOException(e1);
			}
			
			log.info("File path pasado a URI : "+fileUri);
			log.info("File path pasado a URI (protocol) :"+fileUri.getScheme()+", si no es file o es null usa /");
			
		 	
			/*
			 * Si es schema file:// se pueden manejar ficheros grandes usando un patch VFS
			 * Se usan strings para ubicar el fichero en filesystem
			 * Al PDFService el input/out string tiene que ir en formato no URI
			 */
			if(fileUri.getScheme() == null || fileUri.getScheme().equalsIgnoreCase("file"))
			{
				// cogemos el path en formato no URI
				String inputPath = FileUtil.getLocalPathFromURI(fileUri);
				
				File file = new File(pdfParameter.getPath());
				outputPath = PreferencesUtil.getOutputDir(file) + File.separatorChar + PreferencesUtil.getOutputName(file.getName()) + "." + FileUtil.EXTENSION_PDF;
				
				PdfService.sign(inputPath, outputPath, signaturePreferences, ownerPassword);
					
				
			/*
			 * Si no es schema file:// se manipulan los ficheros con streams 
			 * y no se pueden tratar grandes tamaños ya que se hace un volcado a memoria del stream para su manipulación 
			 */
			}else 
			{
				String sss = PreferencesUtil.getOutputNameFromCompletePath(pdfParameter.getPath());
				outputPath = PreferencesUtil.getOutputDir(pdfParameter.getPath()) + "/" + sss + "." + FileUtil.EXTENSION_PDF;
				try {
					PdfService.sign(is, FileUtil.getOutputStreamFromURI(outputPath), signaturePreferences, ownerPassword);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					log.error("error creando el OutputStream asociado al fichero de entrada " + FileUtil.getLocalPathFromURI(pdfParameter.getPath()) , e);
					throw new IOException(e);
				}
			}
						
			// TODO centralizar esto
			// actualizo la entrada de la tabla
			pdfParameter.setPath(outputPath);
			pdfParameter.setSignatures(null);
			String mimeType = FileUtil.getMimeType(outputPath);
			pdfParameter.setMimeType(mimeType);

			// validar
			if (PreferencesUtil.getBoolean(PreferencesUtil.AUTO_VALIDATE)) {
				ValidateController.validate(pdfParameter);
			}

			// mensaje
			String m = MessageFormat.format(LanguageUtil.getLanguage().getString("info.document.signed"), FileUtil.getLocalPathFromURI(pdfParameter.getPath()));
			Display.getDefault().syncExec(new ProgressWriter(ProgressWriter.INFO, m));

		} catch (PdfSignatureException e) {

			String m = MessageFormat.format(LanguageUtil.getLanguage().getString("error.certificate.sign.unexpected"),
					FileUtil.getLocalPathFromURI(pdfParameter.getPath()), e.getMessage());
			log.error(m, e);
			Display.getDefault().syncExec(new ProgressWriter(ProgressWriter.ERROR, m));
		}
	}

	private static void signXades(DocumentInfo pdfParameter, KsSignaturePreferences ksSignaturePreferences) throws OverwritingException,
			IOException, OCSPUnknownUrlException, CertificateExpiredException, CertificateNotYetValidException, RevokedException,
			OCSPCoreException, ConnectionException, OCSPIssuerRequiredException {

		try {
			
			boolean isFacturae = false;
			
			if (pdfParameter.getMimeType() != null && pdfParameter.getMimeType().equals(FileUtil.MIMETYPE_XML)) {
				try {
					InputStream is = FileUtil.getInputStreamFromURI(pdfParameter.getPath());
					isFacturae = isFacturae(is);
					
				} catch (URISyntaxException e) {
					throw new IOException(e);
				}	
			}

			log.info("isFacturae: " + isFacturae);
			
			// firma
			byte[] bytes;
			if (isFacturae) {
				bytes = signFacturae(pdfParameter.getPath(), ksSignaturePreferences);
			} else {
				bytes = signDetached(pdfParameter.getPath(), ksSignaturePreferences);
			}
			
			
			File inputFile = new File(pdfParameter.getPath());
			
			String outputDir = PreferencesUtil.getOutputDir(inputFile);
			
			/*
			 * si el dir está URIEncodeado, encodeamos el nombre, si no lo decodeamos
			 * esto ocurre porque el path de salida puede ser el definido por el usuario (decoded) 
			 * o el del fichero per se (encoded)
			 */
			String outputName = PreferencesUtil.getOutputName(inputFile.getName());
			
			if (FileUtil.isURIEncoded(outputDir)) {
				if (!FileUtil.isURIEncoded(outputName)) {
					outputName = URIEncoder.encode(outputName, "utf-8");
				}
			} else {
				outputName = URIUtil.decode(outputName, "utf-8");
			}
			
			String outputPath = null;
			
			if (pdfParameter.getMimeType() != null && pdfParameter.getMimeType().equals(FileUtil.MIMETYPE_SAR)) {
				// 1- si es un sar el resultante simpre es un sar tambien
				outputPath = outputDir + File.separatorChar + outputName + "." + FileUtil.EXTENSION_SAR;

			} else {
				
				if (isFacturae) {
					// 2- si es un facturae el resultante es siempre un xml
					outputPath = outputDir + File.separatorChar	+ outputName + "." + FileUtil.EXTENSION_XML;
					
				} else {
					// 3- si es un fichero cualquierra depende de la preferencia "XADES_ARCHIVE".  
					// sar
					if (PreferencesUtil.getBoolean(PreferencesUtil.XADES_ARCHIVE)) {
						outputPath = outputDir + File.separatorChar	+ outputName + "." + FileUtil.EXTENSION_SAR;
					// xml
					} else {
						outputPath = outputDir + File.separatorChar	+ outputName + "." + FileUtil.EXTENSION_XML;
						
						/*
						 * unsupported
						 * 
						 * validamos que siendo firma XML (y haciendo una firma detached) el archivo origen y destino no sea el mismo.
						 * // TODO esto se podría hacer con una firma enveloped/ing
						 * @see SignController (core; desde consola no se pueden hacer firmas sar) + SignController (desktop)
						 */
						if (inputFile.getPath().equals(outputPath)){
							log.error("Se está intentando firmar un fichero XML sobre si mismo. Modifique las preferencias para que esto no ocurra (firma detached sar, sufijo o directorio destino diferente)");
							throw new OverwritingException();
						}
					}
				}
			}

			FileUtil.bytesToFile(bytes, outputPath);

			// TODO centralizar esto
			// actualizo la entrada de la tabla
			pdfParameter.setPath(outputPath);
			pdfParameter.setSignatures(null);
			String mimeType = FileUtil.getMimeType(outputPath);
			pdfParameter.setMimeType(mimeType);

			// validar
			if (PreferencesUtil.getBoolean(PreferencesUtil.AUTO_VALIDATE)) {
				ValidateController.validate(pdfParameter);
			}

			// mensaje
			String m = MessageFormat.format(LanguageUtil.getLanguage().getString("info.document.signed"), FileUtil.getLocalPathFromURI(pdfParameter.getPath()));
			Display.getDefault().syncExec(new ProgressWriter(ProgressWriter.INFO, m));

		} catch (XadesSignatureException e) {

			String m = MessageFormat.format(LanguageUtil.getLanguage().getString("error.certificate.sign.unexpected"),
					FileUtil.getLocalPathFromURI(pdfParameter.getPath()), e.getMessage());
			log.error(m, e);
			Display.getDefault().syncExec(new ProgressWriter(ProgressWriter.ERROR, m));
		}
	}

	
	
	private static byte[] signFacturae(String documentPath, KsSignaturePreferences ksSignaturePreferences) throws XadesSignatureException,
			OCSPUnknownUrlException, CertificateExpiredException, CertificateNotYetValidException, RevokedException, OCSPCoreException,
			ConnectionException, OCSPIssuerRequiredException, IOException {

		XadesSignaturePreferences signaturePreferences = getXadesSignaturePreferences(ksSignaturePreferences);
		
		InputStream document;
		try {
			document = FileUtil.getInputStreamFromURI(documentPath);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		} 

		byte[] bytes = XadesService.signFacturae(document, signaturePreferences);
		
		return bytes;
	}
	
	private static byte[] signDetached(String documentPath, KsSignaturePreferences ksSignaturePreferences) throws XadesSignatureException,
			OCSPUnknownUrlException, CertificateExpiredException, CertificateNotYetValidException, RevokedException, OCSPCoreException,
			ConnectionException, OCSPIssuerRequiredException {

		XadesSignaturePreferences signaturePreferences = getXadesSignaturePreferences(ksSignaturePreferences);
		
		byte[] bytes = XadesService.signArchiver(documentPath, signaturePreferences);
		
		return bytes;
	}
	
	private static XadesSignaturePreferences getXadesSignaturePreferences(KsSignaturePreferences ksSignaturePreferences) {
		
		XadesSignaturePreferences signaturePreferences = new XadesSignaturePreferences();
		signaturePreferences.setKsSignaturePreferences(ksSignaturePreferences);
		signaturePreferences.setType(XadesSignaturePreferences.Type.Detached);
		signaturePreferences.setGenerateArchiver(PreferencesUtil.getBoolean(PreferencesUtil.XADES_ARCHIVE));
		signaturePreferences.setKsCache(PreferencesUtil.getCacheKeystoreComplete());
		
		String tsurl = null;
		String tsaOcspUrl = null;
		if (PreferencesUtil.getBoolean(PreferencesUtil.SIGN_TS_ENABLE) == true) {
			tsurl = PreferencesUtil.getTimestampPreferences().get(PreferencesUtil.getString(PreferencesUtil.SIGN_TS_TSA)).getUrl();
			tsaOcspUrl = PreferencesUtil.getTimestampPreferences().get(PreferencesUtil.getString(PreferencesUtil.SIGN_TS_TSA)).getOcspUrl();
		}
		
		signaturePreferences.setTimestampUrl(tsurl);
		signaturePreferences.setTimestampOcspUrl(tsaOcspUrl);
		signaturePreferences.setTimestampUser(null);
		signaturePreferences.setTimestampPassword(null);
		
		
		boolean addOCSP = PreferencesUtil.getBoolean(PreferencesUtil.SIGN_OCSP_ENABLE);
		signaturePreferences.setAddOCSP(addOCSP);
		
		StatisticsUtil.log(StatisticsUtil.KEY_SIGN_OCSP, addOCSP + "");
		log.info("ocsp enable: " + addOCSP);
		
		boolean xlOcspAddAll = PreferencesUtil.getBoolean(PreferencesUtil.XADES_XL_OCSP_ADD_ALL);
		signaturePreferences.setXlOcspAddAll(xlOcspAddAll);
		
		return signaturePreferences;
	}
	
	 // Logica para identificar la version de facturae
	private static boolean isFacturae(InputStream is) {
	    
		boolean isFacturae = false;
		
		try {
	        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	        domFactory.setNamespaceAware(false);
	        DocumentBuilder builder = domFactory.newDocumentBuilder();
	        Document doc = builder.parse(is);
	        XPath xpath = XPathFactory.newInstance().newXPath();        
	        XPathExpression expr = xpath.compile("/Facturae/FileHeader/SchemaVersion");
	        Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
	        
	        if (node != null) {
	        	isFacturae = true;
	        }
	        
		} catch (ParserConfigurationException e) {
			// nada (isFacturae = false)
		} catch (SAXException e) {
			// nada (isFacturae = false)
		} catch (IOException e) {
			// nada (isFacturae = false)
		} catch (XPathExpressionException e) {
			// nada (isFacturae = false)
		}
		
        return isFacturae;
    }

}
