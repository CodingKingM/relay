import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useApi } from '../hooks/useApi'
import { useAuth } from '../hooks/useAuth'
import { httpClient } from '../utils/httpClient'
import PostList from '../components/Posts/PostList'

function UserProfilePage() {
    const { username } = useParams()
    const navigate = useNavigate()
    const { user: currentUser } = useAuth()

    const { data: userInfo, loading: userLoading, error: userError } = useApi(`/users/${username}`)
    const { data: posts, loading: postsLoading, error: postsError } = useApi(`/posts/user/${username}`)

    const [isFollowing, setIsFollowing] = useState(false)
    const [followLoading, setFollowLoading] = useState(false)
    const [followError, setFollowError] = useState(null)

    useEffect(() => {
        const checkFollowStatus = async () => {
            if (!currentUser || currentUser.username === username) return

            try {
                const searchResults = await httpClient.findUsers(username)
                const userResult = searchResults?.find(u => u.username === username)
                if (userResult) {
                    setIsFollowing(userResult.isFollowing)
                }
            } catch (err) {
                console.error('Failed to check follow status:', err)
            }
        }

        checkFollowStatus()
    }, [username, currentUser])

    const handleFollow = async () => {
        setFollowLoading(true)
        setFollowError(null)

        try {
            const newFollowState = !isFollowing
            setIsFollowing(newFollowState)
            if (isFollowing) {
                await httpClient.unfollowUser(username)
            } else {
                await httpClient.followUser(username)
            }

        } catch (err) {
            console.error('Failed to toggle follow:', err)
            setFollowError(err.message)

            setIsFollowing(!isFollowing)

        } finally {
            setFollowLoading(false)
        }
    }

    if (userLoading || postsLoading) {
        return <div className="loading">Loading profile...</div>
    }

    if (userError || !userInfo) {
        return (
            <div className="error-message">
                User not found
                <button
                    onClick={() => navigate('/search')}
                    style={{ marginLeft: '1rem' }}
                    className="form-button"
                >
                    Go to Search
                </button>
            </div>
        )
    }

    const isOwnProfile = currentUser?.username === username

    return (
        <div>
            <div className="profile-card">
                <div className="profile-avatar">{userInfo.username.charAt(0).toUpperCase()}</div>
                <div className="profile-info" style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>{userInfo.username}</div>
                <div className="profile-info" style={{ color: '#555' }}>
                    {userInfo.fullName ? userInfo.fullName : <span style={{ color: '#aaa', fontStyle: 'italic' }}>Full name not set</span>}
                </div>
                <div className="profile-info" style={{ color: '#555' }}>
                    {userInfo.email ? userInfo.email : <span style={{ color: '#aaa', fontStyle: 'italic' }}>Email not set</span>}
                </div>
                <div className="profile-info" style={{ color: '#555', marginBottom: '1rem' }}>
                    {userInfo.biography ? userInfo.biography : <span style={{ color: '#aaa', fontStyle: 'italic' }}>No biography</span>}
                </div>
                <div style={{ display: 'flex', justifyContent: 'center', gap: '2rem', marginBottom: '1rem' }}>
                    <div><b>Followers:</b> {userInfo.followerCount}</div>
                    <div><b>Following:</b> {userInfo.followingCount}</div>
                </div>
                {!isOwnProfile && currentUser && (
                    <div style={{ marginTop: '1rem' }}>
                        <button
                            className={`follow-button ${isFollowing ? 'following' : ''}`}
                            onClick={handleFollow}
                            disabled={followLoading}
                        >
                            {followLoading
                                ? (isFollowing ? 'Unfollowing...' : 'Following...')
                                : (isFollowing ? 'Unfollow' : 'Follow')
                            }
                        </button>
                        {followError && (
                            <div className="error-message" style={{ marginTop: '0.5rem' }}>
                                {followError}
                            </div>
                        )}
                    </div>
                )}
            </div>

            {postsError && (
                <div className="error-message">
                    Error loading posts: {postsError}
                </div>
            )}

            <h2 className="page-title" style={{ textAlign: 'center', marginBottom: '1.5rem' }}>Posts by {username}</h2>
            <PostList posts={posts || []} />
        </div>
    )
}

export default UserProfilePage