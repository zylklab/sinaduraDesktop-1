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
package net.esle.sinadura.gui.events;

import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.text.MessageFormat;
import java.util.List;

import net.esle.sinadura.core.exceptions.ConnectionException;
import net.esle.sinadura.core.exceptions.OCSPCoreException;
import net.esle.sinadura.core.exceptions.OCSPIssuerRequiredException;
import net.esle.sinadura.core.exceptions.OCSPUnknownUrlException;
import net.esle.sinadura.core.exceptions.RevokedException;
import net.esle.sinadura.core.model.PdfSignaturePreferences;
import net.esle.sinadura.core.util.FileUtil;
import net.esle.sinadura.gui.controller.SignController;
import net.esle.sinadura.gui.model.DocumentInfo;
import net.esle.sinadura.gui.util.LanguageUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

class SignProgress implements IRunnableWithProgress {

	private static Log log = LogFactory.getLog(SignProgress.class);

	
	private List<DocumentInfo> pdfParameterList = null;
	private PdfSignaturePreferences pdfSignaturePreferences;
	

	public SignProgress(List<DocumentInfo> pdfParameterList, PdfSignaturePreferences ksSignaturePreferences) {

		this.pdfSignaturePreferences = ksSignaturePreferences;
		this.pdfParameterList = pdfParameterList;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

		try {
			
			for (DocumentInfo pdfParameter : this.pdfParameterList) {
				if (!monitor.isCanceled()) {
					
					String m2;
					m2 = MessageFormat.format(LanguageUtil.getLanguage().getString("info.document.signing"), FileUtil.getLocalPathFromURI(pdfParameter.getPath()));
					monitor.beginTask(m2, IProgressMonitor.UNKNOWN);

					// firma
					SignController.sign(pdfParameter, pdfSignaturePreferences);
				} else {
					throw new InterruptedException("The long running operation was cancelled");
				}
			}
			// estas excepciones interrumpen la firma para el resto de documentos
		} catch (ConnectionException e) {
			throw new InvocationTargetException(e);
		} catch (RevokedException e) {
			throw new InvocationTargetException(e);
		} catch (OCSPIssuerRequiredException e) {
			throw new InvocationTargetException(e);
		} catch (OCSPUnknownUrlException e) {
			throw new InvocationTargetException(e);
		} catch (OCSPCoreException e) {
			throw new InvocationTargetException(e);
		} catch (CertificateExpiredException e) {
			throw new InvocationTargetException(e);
		} catch (CertificateNotYetValidException e) {
			throw new InvocationTargetException(e);
		}

		monitor.done();
	}

}
