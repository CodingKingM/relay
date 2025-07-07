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
    const saveSession = (userData) => {
        localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(userData))
        setCurrentUser(userData)
    }

    // clearSession is simplified
    const clearSession = () => {
        localStorage.removeItem(STORAGE_KEYS.user)
        setCurrentUser(null)
        setAuthError(null)
    }

    const createAccount = async (username, password) => {
        try {
            setAuthError(null)
            setIsLoading(true)
            // Use the new httpClient method
            await httpClient.registerUser(username, password)
            // For a better user experience, automatically sign in after registration
            return await signIn(username, password)
        } catch (error) {
            setAuthError(error.message)
            return { success: false, error: error.message }
        } finally {
            setIsLoading(false)
        }
    }

    const signIn = async (username, password) => {
        try {
            setAuthError(null)
            setIsLoading(true)
            // Use the dedicated authentication method from httpClient
            const userData = await httpClient.authenticateUser(username, password)
            // Save the returned user data to the session
            saveSession(userData)
            return { success: true }
        } catch (error) {
            setAuthError(error.message)
            return { success: false, error: error.message }
        } finally {
            setIsLoading(false)
        }
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

    // The value provided to the context is now cleaner
    const value = {
        currentUser,
        isLoading,
        authError,
        // Authentication status now depends only on currentUser
        isAuthenticated: !!currentUser,
        createAccount,
        signIn,
        signOut,
        resetError,

        // Aliases for convenience
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