// HTTP client utility developed with claude ai assistance for API communication patterns
// Used ai for request handling, authentication and error management strategies
const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

/**
 * The core request function. It's now configured to always send cookies.
 * @param {string} endpoint The API endpoint to call.
 * @param {object} options The options for the fetch call.
 */
export async function makeRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`

    // ADD: Debug logging to see what requests are being made
    console.log(`🚀 Making request to: ${url}`, {
        method: options.method || 'GET',
        headers: options.headers,
        credentials: 'include'
    })

    // Configuration for the fetch request.
    // 'credentials: "include"' is the key change that tells the browser
    // to send cookies with the request.
    const config = {
        ...options,
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            ...options.headers,
        },
    }

    try {
        const response = await fetch(url, config)

        // ADD: Log response status and check for session cookie
        console.log(`📡 Response: ${response.status}`, {
            url,
            status: response.status,
            sessionCookie: document.cookie.includes('JSESSIONID') ? 'Present' : 'Missing'
        })

        // Error handling remains the same
        if (!response.ok) {
            if (response.status === 401) {
                console.error('❌ Authentication failed - 401. Session cookie:',
                    document.cookie.includes('JSESSIONID') ? 'Present' : 'Missing')
                throw new Error('Authentication failed')
            }
            if (response.status === 403) {
                throw new Error('Access denied')
            }
            if (response.status === 404) {
                throw new Error('Resource not found')
            }
            const errorText = await response.text()
            console.error('❌ Request failed:', { url, status: response.status, error: errorText })
            throw new Error(errorText || `Request failed with status ${response.status}`)
        }

        // Response handling remains the same
        if (response.status === 204) {
            console.log('✅ Request successful (no content)')
            return null
        }

        const contentType = response.headers.get('content-type')
        if (contentType && contentType.includes('application/json')) {
            const data = await response.json()
            console.log('✅ Request successful (JSON):', data)
            return data
        }

        const textData = await response.text()
        console.log('✅ Request successful (text):', textData)
        return textData
    } catch (error) {
        console.error('💥 Request error:', { url, error: error.message })
        if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
            throw new Error('Network error - unable to connect to server')
        }
        throw error
    }
}

// The httpClient object is now refactored.
// Most functions are simpler as they don't need to pass credentials.
export const httpClient = {
    // --- AUTHENTICATION-SPECIFIC METHODS ---

    /**
     * Authenticates the user by sending a Basic Auth header.
     * This is one of the few places where we manually create the header.
     */
    authenticateUser: (username, password) => {
        const encoded = btoa(`${username}:${password}`)
        console.log('🔐 Authenticating user:', username)
        return makeRequest('/users/login', {
            method: 'POST',
            headers: {
                'Authorization': `Basic ${encoded}`
            }
        })
    },

    /**
     * Registers a new user.
     */
    registerUser: (username, password) => {
        const encoded = btoa(`${username}:${password}`)
        console.log('📝 Registering user:', username)
        return makeRequest('/users/register', {
            method: 'POST',
            headers: {
                'Authorization': `Basic ${encoded}`
            }
        });
    },

    /**
     * Logs the user out. The browser automatically sends the session cookie
     * which the backend will invalidate.
     */
    signOut: () => {
        console.log('🚪 Signing out user')
        return makeRequest('/users/logout', { method: 'POST' });
    },

    // --- GENERAL API METHODS ---
    // These no longer need a 'credentials' parameter.

    get: (endpoint) => makeRequest(endpoint, { method: 'GET' }),
    post: (endpoint, data, options = {}) => {
        if (options.raw) {
            return makeRequest(endpoint, {
                method: 'POST',
                body: data,
                headers: { 'Content-Type': 'text/plain' }
            });
        }
        return makeRequest(endpoint, {
            method: 'POST',
            body: data ? JSON.stringify(data) : undefined,
        });
    },
    put: (endpoint, data) => makeRequest(endpoint, {
        method: 'PUT',
        body: JSON.stringify(data),
    }),
    delete: (endpoint) => makeRequest(endpoint, { method: 'DELETE' }),

    findUsers: (query) => {
        console.log('🔍 Searching for users:', query)
        return makeRequest(`/users/search?q=${encodeURIComponent(query)}`)
    },
    followUser: (username) => {
        console.log('👥 Following user:', username)
        return makeRequest(`/users/${username}/follow`, { method: 'POST' })
    },
    unfollowUser: (username) => {
        console.log('💔 Unfollowing user:', username)
        return makeRequest(`/users/${username}/follow`, { method: 'DELETE' })
    },
    getFollowers: (username) => {
        console.log('👥 Getting followers for:', username)
        return makeRequest(`/users/${username}/followers`)
    },
    getFollowing: (username) => {
        console.log('👥 Getting following for:', username)
        return makeRequest(`/users/${username}/following`)
    },
    publishPost: (content) => makeRequest('/posts', {
        method: 'POST',
        body: JSON.stringify({ content }),
    }),
    getTimeline: () => makeRequest('/posts/timeline'),
    getUserPosts: (username) => makeRequest(`/posts/user/${username}`),
    likePost: (postId) => makeRequest(`/posts/${postId}/like`, { method: 'POST' }),
    unlikePost: (postId) => makeRequest(`/posts/${postId}/like`, { method: 'DELETE' })
}