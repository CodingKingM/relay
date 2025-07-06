import { useState, useEffect } from 'react'
import { httpClient } from '../utils/httpClient'

export function useApi(endpoint, dependencies = []) {
    const [data, setData] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        if (!endpoint) {
            setLoading(false)
            return
        }

        const fetchData = async () => {
            try {
                setLoading(true)
                setError(null)
                const result = await httpClient.get(endpoint)
                setData(result)
            } catch (err) {
                setError(err.message)
                setData(null)
            } finally {
                setLoading(false)
            }
        }

        fetchData()
    }, [endpoint, ...dependencies])

    return { data, loading, error }
}

export function useApiCall() {
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState(null)

    const call = async (endpoint, method = 'GET', payload = null) => {
        setLoading(true)
        setError(null)

        try {
            let result

            switch (method.toUpperCase()) {
                case 'GET':
                    result = await httpClient.get(endpoint)
                    break
                case 'POST':
                    result = await httpClient.post(endpoint, payload)
                    break
                case 'PUT':
                    result = await httpClient.put(endpoint, payload)
                    break
                case 'DELETE':
                    result = await httpClient.delete(endpoint)
                    break
                default:
                    throw new Error(`Unsupported method: ${method}`)
            }

            return result
        } catch (err) {
            setError(err.message)
            throw err
        } finally {
            setLoading(false)
        }
    }

    const clearError = () => setError(null)

    return { call, loading, error, clearError }
}


export function useApiLegacy() {
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState(null)

    const executeRequest = async (apiCall) => {
        try {
            setLoading(true)
            setError(null)
            const result = await apiCall()
            return { data: result, success: true }
        } catch (err) {
            setError(err.message)
            return { success: false, error: err.message }
        } finally {
            setLoading(false)
        }
    }

    const searchUsers = (query) =>
        executeRequest(() => httpClient.findUsers(query))

    const toggleFollow = (username, isFollowing) =>
        executeRequest(() =>
            isFollowing
                ? httpClient.unfollowUser(username)
                : httpClient.followUser(username)
        )

    const createPost = (content) =>
        executeRequest(() => httpClient.publishPost(content))

    const fetchTimeline = () =>
        executeRequest(() => httpClient.getTimeline())

    const fetchUserPosts = (username) =>
        executeRequest(() => httpClient.getUserPosts(username))

    const toggleLike = (postId, isLiked) =>
        executeRequest(() =>
            isLiked
                ? httpClient.unlikePost(postId)
                : httpClient.likePost(postId)
        )

    const clearError = () => setError(null)

    return {
        loading,
        error,
        searchUsers,
        toggleFollow,
        createPost,
        fetchTimeline,
        fetchUserPosts,
        toggleLike,
        clearError
    }
}