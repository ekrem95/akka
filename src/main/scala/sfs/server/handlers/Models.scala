package sfs.server.handlers

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

object Models {
  // domain models
  final case class errorResponse(statusCode: Int, error: String, message: String)
  final case class findClient(id: String, c_type: String)
  final case class findAccessToken(id: String, client_id: String, scopes: String, secret: String, user_id: Int)
  final case class foundFile(id: Int, name: String, algorithms: String)
  final case class loginModel(email: String, password: String)
  final case class registerModel(email: String, name: String, password: String)
  final case class registerResponse(access_token: String)

  // formats for unmarshalling and marshalling
  implicit val errorResponseFormat: RootJsonFormat[errorResponse] = jsonFormat3(errorResponse)
  implicit val findClientFormat: RootJsonFormat[findClient] = jsonFormat2(findClient)
  implicit val findAccessTokenFormat: RootJsonFormat[findAccessToken] = jsonFormat5(findAccessToken)
  implicit val findFilesFormat: RootJsonFormat[foundFile] = jsonFormat3(foundFile)
  implicit val loginModelFormat: RootJsonFormat[loginModel] = jsonFormat2(loginModel)
  implicit val registerModelFormat: RootJsonFormat[registerModel] = jsonFormat3(registerModel)
  implicit val registerResponseFormat: RootJsonFormat[registerResponse] = jsonFormat1(registerResponse)
}
