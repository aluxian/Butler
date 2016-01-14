package com.butler.cloud.models.data

/**
 * @param authToken The authentication token the client should use for future requests.
 * @param error The error message (if there was an error).
 */
case class ResponseData(authToken: String = null,
                        error: String = null)
