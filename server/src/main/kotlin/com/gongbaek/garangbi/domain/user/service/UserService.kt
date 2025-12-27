package com.gongbaek.garangbi.domain.user.service

import com.gongbaek.garangbi.domain.user.dto.request.LoginRequest
import com.gongbaek.garangbi.domain.user.dto.request.RegisterRequest
import com.gongbaek.garangbi.domain.user.dto.response.AuthResponse
import com.gongbaek.garangbi.domain.user.repository.UserRepository
import com.gongbaek.garangbi.global.exception.BusinessException
import com.gongbaek.garangbi.global.exception.ErrorCode
import com.gongbaek.garangbi.global.security.JwtProvider

class UserService(
    private val userRepository: UserRepository = UserRepository(),
) {
    fun register(request: RegisterRequest): AuthResponse {
        validateUserNotExists(request.firebaseUid, request.email)

        val user =
            userRepository.save(
                firebaseUid = request.firebaseUid,
                email = request.email,
                displayName = request.displayName,
            )

        val token = JwtProvider.generateToken(user.id.toString())

        return AuthResponse(
            token = token,
            userId = user.id.toString(),
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val user =
            userRepository.findByFirebaseUid(request.firebaseUid)
                ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        val token = JwtProvider.generateToken(user.id.toString())

        return AuthResponse(
            token = token,
            userId = user.id.toString(),
        )
    }

    private fun validateUserNotExists(
        firebaseUid: String,
        email: String,
    ) {
        if (userRepository.existsByFirebaseUid(firebaseUid)) {
            throw BusinessException(ErrorCode.USER_ALREADY_EXISTS)
        }
        if (userRepository.existsByEmail(email)) {
            throw BusinessException(ErrorCode.USER_ALREADY_EXISTS)
        }
    }
}
