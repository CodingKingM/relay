// Authentication hook developed with claude ai assistance for session management patterns
// Used ai for react hook structure and state persistence strategies
import { useState, useEffect, createContext, useContext } from 'react'
import { httpClient } from '../utils/httpClient'

// We no longer need to store credentials
const STORAGE_KEYS = {
    user: 'relay_current_user',
}

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
    const [currentUser, setCurrentUser] = useState(null)
    // The 'authData' state is no longer needed
    const [isLoading, setIsLoading] = useState(true)
    const [authError, setAuthError] = useState(null)

    useEffect(() => {
        // On initial load, we only check for the user object
        const savedUser = localStorage.getItem(STORAGE_KEYS.user)

        if (savedUser) {
            try {
                setCurrentUser(JSON.parse(savedUser))
            } catch {
                // If parsing fails, clear the session
                clearSession()
            }
        }
        setIsLoading(false)
    }, [])

    // saveSession no longer handles credentials
    const saveSession = (userData, token) => {
        localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(userData))
        if (token) localStorage.setItem('relay_token', token)
        setCurrentUser(userData)
    }

    const clearSession = () => {
        localStorage.removeItem(STORAGE_KEYS.user)
        localStorage.removeItem('relay_token')
        setCurrentUser(null)
        setAuthError(null)
    }

    const createAccount = async (username, password) => {
        setAuthError(null)
        setIsLoading(true)
        try {
            await httpClient.registerUser(username, password)
            await signIn(username, password)
        } catch (error) {
            setAuthError(error.message)
            setIsLoading(false)
            throw error
        }
        setIsLoading(false)
    }

    const signIn = async (username, password) => {
        setAuthError(null)
        setIsLoading(true)
        try {
            const response = await httpClient.authenticateUser(username, password)
            saveSession(response.user, response.token)
        } catch (error) {
            setAuthError(error.message)
            setIsLoading(false)
            throw error
        }
        setIsLoading(false)
    }

    const signOut = async () => {
        try {
            // Use the new, simpler signOut method
            await httpClient.signOut()
        } catch (error) {
            console.warn('Logout request failed:', error)
        } finally {
            // Always clear the local session
            clearSession()
        }
    }

    const resetError = () => setAuthError(null)

    const value = {
        currentUser,
        isLoading,
        authError,
        isAuthenticated: !!currentUser,
        createAccount,
        signIn,
        signOut,
        resetError,
        user: currentUser,
        loading: isLoading,
        error: authError,
        register: createAccount,
        login: signIn,
        logout: signOut
    }

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
    const context = useContext(AuthContext)
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider')
    }
    return context
}