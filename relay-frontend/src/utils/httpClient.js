const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

async function makeRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`

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

        if (!response.ok) {
            if (response.status === 401) throw new Error('Authentication failed')
            if (response.status === 403) throw new Error('Access denied')
            if (response.status === 404) throw new Error('Resource not found')
            const errorText = await response.text()
            throw new Error(errorText || `Request failed with status ${response.status}`)
        }

        if (response.status === 204) return null

        const contentType = response.headers.get('content-type')
        if (contentType && contentType.includes('application/json')) {
            return response.json()
        }

        return response.text()
    } catch (error) {
        if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
            throw new Error('Network error - unable to connect to server')
        }
        throw error
    }
}

export const httpClient = {
    authenticateUser: (username, password) => {
        const encoded = btoa(`${username}:${password}`)
        return makeRequest('/users/login', {
            method: 'POST',
            headers: { 'Authorization': `Basic ${encoded}` }
        })
    },

    registerUser: (username, password) => {
        const encoded = btoa(`${username}:${password}`)
        return makeRequest('/users/register', {
            method: 'POST',
            headers: { 'Authorization': `Basic ${encoded}` }
        })
    },

    signOut: () => makeRequest('/users/logout', { method: 'POST' }),

    get: (endpoint) => makeRequest(endpoint, { method: 'GET' }),

    post: (endpoint, data, options = {}) => {
        if (options.raw) {
            return makeRequest(endpoint, {
                method: 'POST',
                body: data,
                headers: { 'Content-Type': 'text/plain' }
            })
        }
        return makeRequest(endpoint, {
            method: 'POST',
            body: data ? JSON.stringify(data) : undefined,
        })
    },

    put: (endpoint, data) => makeRequest(endpoint, {
        method: 'PUT',
        body: JSON.stringify(data),
    }),

    delete: (endpoint) => makeRequest(endpoint, { method: 'DELETE' }),

    findUsers: (query) => makeRequest(`/users/search?q=${encodeURIComponent(query)}`),
    followUser: (username) => makeRequest(`/users/${username}/follow`, { method: 'POST' }),
    unfollowUser: (username) => makeRequest(`/users/${username}/follow`, { method: 'DELETE' }),
    getFollowers: (username) => makeRequest(`/users/${username}/followers`),
    getFollowing: (username) => makeRequest(`/users/${username}/following`),
    publishPost: (content) => makeRequest('/posts', {
        method: 'POST',
        body: JSON.stringify({ content }),
    }),
    getTimeline: () => makeRequest('/posts/timeline'),
    getUserPosts: (username) => makeRequest(`/posts/user/${username}`),
    likePost: (postId) => makeRequest(`/posts/${postId}/like`, { method: 'POST' }),
    unlikePost: (postId) => makeRequest(`/posts/${postId}/like`, { method: 'DELETE' }),
}
