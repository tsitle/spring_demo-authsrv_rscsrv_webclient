package com.ts.springdemo.authserver.service

import com.nimbusds.jose.util.Base64
import org.springframework.stereotype.Service
import org.springframework.util.Assert
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


class EncryptionService {

	companion object {
		// for more information see
		//   https://docs.oracle.com/javase/9/docs/specs/security/standard-names.html#secretkeyfactory-algorithms
		private const val KEY_ALGO = "AES"
		private const val KEY_SIZE = 16 * 8
		// for more information see
		//   https://docs.oracle.com/javase/9/docs/specs/security/standard-names.html#secretkeyfactory-algorithms
		private const val KEY_FACTORY_ALGO = "PBKDF2WithHmacSHA256"
		// for more information see
		//   https://docs.oracle.com/javase/9/docs/specs/security/standard-names.html#cipher-algorithm-names
		private const val CIPHER_ALGO = "AES/CBC/PKCS5Padding"
		private const val B64_IV_DELIMITER = ";#IVEND#;"
		private const val B64_SALT_DELIMITER = ";#SALTEND#;"

		private fun getRandomBytes(len: Int): ByteArray {
			val byArr = ByteArray(len)
			val random = SecureRandom()
			random.nextBytes(byArr)
			return byArr
		}

		private fun getPasswordBasedKey(saltStr: String?, password: String): Map<String, Any> {
			val res = mutableMapOf<String, Any>()
			val saltByArr: ByteArray
			if (saltStr == null) {
				saltByArr = getRandomBytes(100)
				res["pwSalt"] = Base64.encode(saltByArr).toString()
			} else {
				saltByArr = Base64(saltStr).decode()
				res["pwSalt"] = "-"
			}
			val pbeKeySpec = PBEKeySpec(password.toCharArray(), saltByArr, 1000, KEY_SIZE)
			val pbeKey = SecretKeyFactory.getInstance(KEY_FACTORY_ALGO).generateSecret(pbeKeySpec)
			res["secretKey"] = SecretKeySpec(pbeKey.encoded, KEY_ALGO)
			return res
		}

		private fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }.uppercase()

		@Suppress("unused")
		fun getMd5(dataStr: String): String {
			return getMd5(dataStr.toByteArray())
		}

		@Suppress("WeakerAccess")
		fun getMd5(dataByArr: ByteArray): String {
			return getDigest(dataByArr, "MD5")
		}

		@Suppress("unused")
		fun getSha256(dataStr: String): String {
			return getSha256(dataStr.toByteArray())
		}

		@Suppress("WeakerAccess")
		fun getSha256(dataByArr: ByteArray): String {
			return getDigest(dataByArr, "SHA256")
		}

		fun encrypt(input: String, password: String): String {
			Assert.hasText(password, "password cannot be empty")

			val pbkData: Map<String, Any> = getPasswordBasedKey(null, password)
			val key: SecretKey = pbkData["secretKey"] as SecretKey
			val ivByArr = getRandomBytes(16)
			val ivParameterSpec = IvParameterSpec(ivByArr)
			val cipher: Cipher = Cipher.getInstance(CIPHER_ALGO)
			cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec)
			val cipherText: ByteArray = cipher.doFinal(input.toByteArray())
			return Base64.encode(ivByArr).toString() + B64_IV_DELIMITER +
					(pbkData["pwSalt"] as String) + B64_SALT_DELIMITER +
					Base64.encode(cipherText).toString()
		}

		@Throws(IllegalStateException::class)
		fun decrypt(cipherTextWithIvAndSalt: String, password: String): String {
			Assert.hasText(password, "password cannot be empty")

			val splitOneArr = cipherTextWithIvAndSalt.split(B64_IV_DELIMITER)
			if (splitOneArr.size != 2) {
				throw IllegalStateException("invalid ciphertext")
			}
			val ivByArr: ByteArray = Base64(splitOneArr[0]).decode()

			val splitTwoArr = splitOneArr[1].split(B64_SALT_DELIMITER)
			if (splitTwoArr.size != 2) {
				throw IllegalStateException("invalid ciphertext")
			}
			val cipherSalt = splitTwoArr[0]
			val cipherText = splitTwoArr[1]

			val pbkData: Map<String, Any> = getPasswordBasedKey(cipherSalt, password)
			val key: SecretKey = pbkData["secretKey"] as SecretKey
			val ivParameterSpec = IvParameterSpec(ivByArr)
			val cipher = Cipher.getInstance(CIPHER_ALGO)
			cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec)
			val plainText = cipher.doFinal(
					Base64.from(cipherText).decode()
				)
			return String(plainText)
		}

		private fun getDigest(dataByArr: ByteArray, algo: String): String {
			val md: MessageDigest = MessageDigest.getInstance(algo)
			md.update(dataByArr)
			return md.digest().toHex()
		}
	}
}
