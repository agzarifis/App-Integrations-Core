/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.provisioning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.Certificate;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.util.Locale;

/**
 * Service class responsible for generating application private keys, public keys and certificates.
 *
 * Created by rsanchez on 11/08/17.
 */
@Service
public class AppKeyPairService extends KeyPairService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppKeyPairService.class);

  private static final String OPENSSL_GEN_KEY_CMD = "openssl genrsa -out %s 1024";

  private static final String OPENSSL_PKCS8_CMD = "openssl pkcs8 -topk8 -nocrypt -in %s -out %s";

  private static final String OPENSSL_PUB_KEY_CMD = "openssl rsa -pubout -in %s -out %s";

  private static final String OPENSSL_GEN_CERT_CMD = "openssl x509 -req -sha256 -days 3650 -in %s"
      + " -CA %s -passin pass:%s -out %s -CAkey %s -set_serial 0x%s";

  private static final String OPENSSL_PKCS12_CMD = "openssl pkcs12 -export -out %s -in %s"
      + " -inkey %s -passout pass:%s";

  private final IntegrationUtils utils;

  private final IntegrationProperties properties;

  public AppKeyPairService(ApplicationArguments args, LogMessageSource logMessage,
      IntegrationUtils utils, IntegrationProperties properties) {
    super(args, logMessage);
    this.utils = utils;
    this.properties = properties;
  }

  /**
   * Export application certificate
   *
   * @param application Application object
   */
  public void exportCertificate(Application application) {
    if (shouldGenerateCertificate() && (application.getAppKeystore() != null)) {
      String keyFileName = generatePrivateKey(application);
      String reqFileName = generateCSR(application, keyFileName);
      generateCertificate(application, keyFileName, reqFileName);
      generatePublicKey(application, keyFileName);
    }
  }

  /**
   * Generate application private key.
   *
   * @param application Application object
   * @return Private key filename
   */
  private String generatePrivateKey(Application application) {
    LOGGER.info("Generating application private key: {}", application.getComponent());

    String appKeyFilename = String.format("%s/%s_app_key.pem", System.getProperty("java.io.tmpdir"),
        application.getId());

    String genKeyPairCommand = String.format(OPENSSL_GEN_KEY_CMD, appKeyFilename);
    executeProcess(genKeyPairCommand);

    String appPKCS8Filename = utils.getCertsDirectory() + application.getId() + "_app.pkcs8";

    String genPkcs8Command = String.format(OPENSSL_PKCS8_CMD, appKeyFilename, appPKCS8Filename);
    executeProcess(genPkcs8Command);

    return appKeyFilename;
  }

  /**
   * Generate the Certificate Signing Request (CSR).
   * @param application Application object
   * @param appKeyFilename Key filename
   * @return Request filename
   */
  private String generateCSR(Application application, String appKeyFilename) {
    LOGGER.info("Generating certificate signing request: {}", application.getComponent());

    String subject =
        String.format("/CN=%s/O=%s/C=%s", application.getComponent(), DEFAULT_ORGANIZATION,
            Locale.US.getCountry());

    String appReqFilename = String.format("%s/%s_app_req.pem", System.getProperty("java.io.tmpdir"),
        application.getId());

    String[] genCSRCommand = new String[] { "openssl", "req", "-new", "-key", appKeyFilename,
        "-subj", subject, "-out", appReqFilename };

    executeProcess(genCSRCommand);

    return appReqFilename;
  }

  /**
   * Generate the application certificate.
   *
   * @param application Application object
   * @param appKeyFilename Key filename
   * @param appReqFilename Request filename
   * @return App certificate filename
   */
  private void generateCertificate(Application application, String appKeyFilename, String appReqFilename) {
    LOGGER.info("Generating application certificate: {}", application.getComponent());

    Certificate certificateInfo = properties.getSigningCert();

    String caCertFilename = certificateInfo.getCaCertFile();
    String caCertKeyFilename = certificateInfo.getCaKeyFile();
    String password = certificateInfo.getCaKeyPassword();
    String sn = Long.toHexString(System.currentTimeMillis());

    String appCertFilename = utils.getCertsDirectory() + application.getId() + "_app.pem";

    String genCerticateCommand =
        String.format(OPENSSL_GEN_CERT_CMD, appReqFilename, caCertFilename, password,
            appCertFilename, caCertKeyFilename, sn);

    executeProcess(genCerticateCommand);

    String passOutput = application.getAppKeystore().getPassword();
    String appPKCS12Filename = utils.getCertsDirectory() + application.getId() + "_app.p12";

    String genPKCS12Command = String.format(OPENSSL_PKCS12_CMD, appPKCS12Filename,
        appCertFilename, appKeyFilename, passOutput);
    executeProcess(genPKCS12Command);
  }

  /**
   * Generate application public key
   * @param application Application object
   * @param appKeyFilename Application key filename
   */
  private void generatePublicKey(Application application, String appKeyFilename) {
    LOGGER.info("Generating public key: {}", application.getComponent());

    String appPubKeyFilename = utils.getCertsDirectory() + application.getId() + "_app_pub.pem";

    String genPublicKeyCommand = String.format(OPENSSL_PUB_KEY_CMD, appKeyFilename, appPubKeyFilename);

    executeProcess(genPublicKeyCommand);
  }
}
