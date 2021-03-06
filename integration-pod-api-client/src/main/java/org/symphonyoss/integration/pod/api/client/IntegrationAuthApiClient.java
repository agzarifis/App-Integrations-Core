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

package org.symphonyoss.integration.pod.api.client;

import static org.symphonyoss.integration.pod.api.client.BaseIntegrationInstanceApiClient
    .INTEGRATION_ID;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING_SOLUTION;

import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.authentication.api.model.AppToken;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.authentication.ForbiddenAuthException;
import org.symphonyoss.integration.exception.authentication.MissingRequiredParameterException;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.exception.authentication.UnexpectedAuthException;
import org.symphonyoss.integration.exception.config.NotFoundException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.pod.api.model.UserAuthorizationDataList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

/**
 * Part of Integration API, holds all endpoints to maintain the integration auth data.
 *
 * Created by rsanchez on 26/07/17.
 */
public class IntegrationAuthApiClient extends BasePodApiClient {

  private static final String GET_USER_AUTH_DATA = "getUserAuthData";

  private static final String UNAUTHORIZED_MESSAGE = "integration.pod.api.client.auth.unauthorized";
  private static final String UNAUTHORIZED_MESSAGE_SOLUTION = UNAUTHORIZED_MESSAGE + ".solution";
  private static final String FORBIDDEN_MESSAGE = "integration.pod.api.client.auth.forbidden";
  private static final String FORBIDDEN_MESSAGE_SOLUTION = FORBIDDEN_MESSAGE + ".solution";
  private static final String API_MSG = "integration.pod.api.unexpected.exception";
  private static final String API_MSG_SOLUTION = API_MSG + ".solution";


  private static final String USER_DATA = "userData";
  private static final String USER_ID = "userId";
  private static final String URL = "url";
  private static final String TOKEN_APPLICATION = "tokenApplication";
  private static final String TOKEN_SYMPHONY = "tokenSymphony";

  private HttpApiClient apiClient;

  public IntegrationAuthApiClient(HttpApiClient apiClient, LogMessageSource logMessage) {
    this.apiClient = apiClient;
    this.logMessage = logMessage;
  }

  /**
   * Save user authorization data.
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier
   * @param userData User authorization data.
   * @throws UnauthorizedUserException User credentials not provided
   * @throws ForbiddenAuthException User not authorized to retrieve user authentication
   * @throws RemoteApiException Unexpected error calling API
   */
  public void saveUserAuthData(String sessionToken, String integrationId,
      UserAuthorizationData userData) throws RemoteApiException {
    checkAuthToken(sessionToken);

    checkParam(integrationId, INTEGRATION_ID);
    checkParam(userData, USER_DATA);

    String path = "/v1/configuration/" + apiClient.escapeString(integrationId) + "/auth/user";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    try {
      apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), userData,
          UserAuthorizationData.class);
    } catch (RemoteApiException e) {
      if (e.getCode() == Response.Status.UNAUTHORIZED.getStatusCode()) {
        String message = logMessage.getMessage(UNAUTHORIZED_MESSAGE);
        String solution = logMessage.getMessage(UNAUTHORIZED_MESSAGE_SOLUTION);
        throw new UnauthorizedUserException(message, solution);
      }

      if (e.getCode() == Response.Status.FORBIDDEN.getStatusCode()) {
        String message = logMessage.getMessage(FORBIDDEN_MESSAGE);
        String solution = logMessage.getMessage(FORBIDDEN_MESSAGE_SOLUTION);
        throw new ForbiddenAuthException(message, solution);
      }

      throw new UnexpectedAuthException(logMessage.getMessage(API_MSG), e,
          logMessage.getMessage(API_MSG_SOLUTION));
    }
  }

  /**
   * Get user authorization data.
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier
   * @param userId User identifier
   * @param url Integration URL
   * @return User authorization data
   * @throws UnauthorizedUserException User credentials not provided
   * @throws ForbiddenAuthException User not authorized to retrieve user authentication
   * @throws RemoteApiException Unexpected error calling API
   */
  public UserAuthorizationData getUserAuthData(String sessionToken, String integrationId,
      Long userId, String url) throws RemoteApiException {
    checkAuthToken(sessionToken);

    checkParam(integrationId, INTEGRATION_ID);
    checkParam(userId, USER_ID);
    checkParam(url, URL);

    String path = "/v1/configuration/" + apiClient.escapeString(integrationId) + "/auth/user";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(USER_ID, String.valueOf(userId));
    queryParams.put(URL, url);

    try {
      return apiClient.doGet(path, headerParams, queryParams, UserAuthorizationData.class);
    } catch (RemoteApiException e) {
      if (e.getCode() == Response.Status.UNAUTHORIZED.getStatusCode()) {
        String message = logMessage.getMessage(UNAUTHORIZED_MESSAGE);
        String solution = logMessage.getMessage(UNAUTHORIZED_MESSAGE_SOLUTION);
        throw new UnauthorizedUserException(message, solution);
      }

      if (e.getCode() == Response.Status.FORBIDDEN.getStatusCode()) {
        String message = logMessage.getMessage(FORBIDDEN_MESSAGE);
        String solution = logMessage.getMessage(FORBIDDEN_MESSAGE_SOLUTION);
        throw new ForbiddenAuthException(message, solution);
      }

      throw new UnexpectedAuthException(logMessage.getMessage(API_MSG), e,
          logMessage.getMessage(API_MSG_SOLUTION));
    }
  }

  /**
   * Search for user authorization data.
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier
   * @param filter Map used as filter.
   * @return List of found user authorization data
   * @throws UnauthorizedUserException User credentials not provided
   * @throws ForbiddenAuthException User not authorized to retrieve user authentication
   * @throws RemoteApiException Unexpected error calling API
   */
  public List<UserAuthorizationData> searchUserAuthData(String sessionToken, String integrationId,
      Map<String, String> filter) throws RemoteApiException {
    checkAuthToken(sessionToken);
    checkParam(integrationId, INTEGRATION_ID);

    String path =
        "/v1/configuration/" + apiClient.escapeString(integrationId) + "/auth/user/search";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    try {
      return apiClient.doGet(path, headerParams, filter, UserAuthorizationDataList.class);
    } catch (RemoteApiException e) {
      if (e.getCode() == Response.Status.UNAUTHORIZED.getStatusCode()) {
        String message = logMessage.getMessage(UNAUTHORIZED_MESSAGE);
        String solution = logMessage.getMessage(UNAUTHORIZED_MESSAGE_SOLUTION);
        throw new UnauthorizedUserException(message, solution);
      }

      if (e.getCode() == Response.Status.FORBIDDEN.getStatusCode()) {
        String message = logMessage.getMessage(FORBIDDEN_MESSAGE);
        String solution = logMessage.getMessage(FORBIDDEN_MESSAGE_SOLUTION);
        throw new ForbiddenAuthException(message, solution);
      }

      throw new UnexpectedAuthException(logMessage.getMessage(API_MSG), e,
          logMessage.getMessage(API_MSG_SOLUTION));
    }
  }

  /**
   * Check HTTP parameter. Throws an {@link RemoteApiException} if the parameter is null.
   * @param param Parameter object
   * @param paramName Parameter name
   * @throws NotFoundException
   */
  private void checkParam(Object param, String paramName) {
    if (param == null) {
      String reason =
          logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING, paramName, GET_USER_AUTH_DATA);
      String solution = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING_SOLUTION, paramName);
      throw new MissingRequiredParameterException(reason, solution);
    }
  }

  /**
   * Save application authentication data.
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier
   * @param appToken Pair of tokens - application one and Symphony one.
   * @throws UnauthorizedUserException User credentials not provided
   * @throws ForbiddenAuthException User not authorized to retrieve user authentication
   */
  public void saveAppAuthenticationToken(String sessionToken, String integrationId,
      AppToken appToken) {
    try {
      checkAuthToken(sessionToken);

      checkParam(integrationId, INTEGRATION_ID);
      checkParam(appToken.getAppToken(), TOKEN_APPLICATION);
      checkParam(appToken.getSymphonyToken(), TOKEN_SYMPHONY);
      // Only AppToken and SymphonyToken are supported by this API
      AppToken appTokenWithoutAppId = new AppToken(null, appToken.getAppToken(),
          appToken.getSymphonyToken());

      String path =
          "/v1/configuration/" + apiClient.escapeString(integrationId) + "/authentication/";

      Map<String, String> headerParams = new HashMap<>();
      headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

      apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), appTokenWithoutAppId,
          AppToken.class);
    } catch (RemoteApiException e) {
      if (e.getCode() == Response.Status.UNAUTHORIZED.getStatusCode()) {
        String message = logMessage.getMessage(UNAUTHORIZED_MESSAGE);
        String solution = logMessage.getMessage(UNAUTHORIZED_MESSAGE_SOLUTION);
        throw new UnauthorizedUserException(message, solution);
      }

      if (e.getCode() == Response.Status.FORBIDDEN.getStatusCode()) {
        String message = logMessage.getMessage(FORBIDDEN_MESSAGE);
        String solution = logMessage.getMessage(FORBIDDEN_MESSAGE_SOLUTION);
        throw new ForbiddenAuthException(message, solution);
      }

      throw new UnexpectedAuthException(logMessage.getMessage(API_MSG), e,
          logMessage.getMessage(API_MSG_SOLUTION));
    }
  }

  /**
   * Find application authentication data.
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier
   * @param applicationToken Application token.
   * @throws UnauthorizedUserException User credentials not provided
   * @throws ForbiddenAuthException User not authorized to retrieve user authentication
   */
  public AppToken getAppAuthenticationToken(String sessionToken, String integrationId,
      String applicationToken) {
    try {
      checkAuthToken(sessionToken);

      checkParam(integrationId, INTEGRATION_ID);
      checkParam(applicationToken, TOKEN_APPLICATION);

      String path = "/v1/configuration/" + apiClient.escapeString(integrationId) + "/authentication/"
          + applicationToken;

      Map<String, String> headerParams = new HashMap<>();
      headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

      return apiClient.doGet(path, headerParams, Collections.<String, String>emptyMap(),
          AppToken.class);
    } catch (RemoteApiException e) {
      if (e.getCode() == Response.Status.NOT_FOUND.getStatusCode()) {
        return null;
      }

      if (e.getCode() == Response.Status.UNAUTHORIZED.getStatusCode()) {
        String message = logMessage.getMessage(UNAUTHORIZED_MESSAGE);
        String solution = logMessage.getMessage(UNAUTHORIZED_MESSAGE_SOLUTION);
        throw new UnauthorizedUserException(message, solution);
      }

      if (e.getCode() == Response.Status.FORBIDDEN.getStatusCode()) {
        String message = logMessage.getMessage(FORBIDDEN_MESSAGE);
        String solution = logMessage.getMessage(FORBIDDEN_MESSAGE_SOLUTION);
        throw new ForbiddenAuthException(message, solution);
      }

      throw new UnexpectedAuthException(logMessage.getMessage(API_MSG), e,
          logMessage.getMessage(API_MSG_SOLUTION));
    }
  }
}
