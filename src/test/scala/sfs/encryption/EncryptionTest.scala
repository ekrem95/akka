package sfs.encryption

import java.util.UUID
import org.scalatest.FunSuite


class EncryptionTest extends FunSuite {
  private val KEY: String = "key"
  private val initialValue: String = UUID.randomUUID.toString

  test("Encryption.encrypt") {
    val encrypted = Encryption.encrypt(KEY, initialValue)
    assert(encrypted !== initialValue)
  }

  test("Encryption.decrypt") {
    val encrypted = Encryption.encrypt(KEY, initialValue)
    assert(encrypted !== initialValue)

    assert(Encryption.decrypt(KEY, encrypted) === initialValue)
  }
}
