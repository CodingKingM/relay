import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useApi } from '../hooks/useApi'
import { useAuth } from '../hooks/useAuth'
import { httpClient } from '../utils/httpClient'
import PostList from '../components/Posts/PostList'
import FollowersFollowing from '../components/Users/FollowersFollowing'

function UserProfilePage() {
    const { username } = useParams()
    const navigate = useNavigate()
    const { user: currentUser } = useAuth()

    const { data: userInfo, loading: userLoading, error: userError } = useApi(`/users/${username}`)
    const { data: posts, loading: postsLoading, error: postsError } = useApi(`/posts/user/${username}`)

    const [isFollowing, setIsFollowing] = useState(false)
    const [followLoading, setFollowLoading] = useState(false)
    const [followError, setFollowError] = useState(null)
    const [showFollowersFollowing, setShowFollowersFollowing] = useState(false)
    const [userInfoState, setUserInfoState] = useState(null)

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

    useEffect(() => {
        if (userInfo) setUserInfoState(userInfo)
    }, [userInfo])

    const handleFollow = async () => {
        setFollowLoading(true)
        setFollowError(null)

        try {
            const newFollowState = !isFollowing
            setIsFollowing(newFollowState)
            setUserInfoState(prev => prev ? {
                ...prev,
                followerCount: prev.followerCount + (newFollowState ? 1 : -1)
            } : prev)
            if (isFollowing) {
                await httpClient.unfollowUser(username)
            } else {
                await httpClient.followUser(username)
            }
        } catch (err) {
            console.error('Failed to toggle follow:', err)
            setFollowError(err.message)
            setIsFollowing(!isFollowing)
            setUserInfoState(prev => prev ? {
                ...prev,
                followerCount: prev.followerCount + (isFollowing ? 1 : -1)
            } : prev)
        } finally {
            setFollowLoading(false)
        }
    }

    const handleFollowersFollowingClose = () => {
        setShowFollowersFollowing(false)
    }

    if (userLoading || postsLoading) {
        return <div className="loading">Loading profile...</div>
    }

    if (userError || !userInfo) {
        return (
            <div className="error-message">
                User not found or profile could not be loaded.
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
                <div className="profile-avatar">{(userInfoState || userInfo).username.charAt(0).toUpperCase()}</div>
                <div className="profile-info" style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>{(userInfoState || userInfo).username}</div>
                <div className="profile-info" style={{ color: '#555' }}>
                    {(userInfoState || userInfo).fullName ? (userInfoState || userInfo).fullName : <span style={{ color: '#aaa', fontStyle: 'italic' }}>Full name not set</span>}
                </div>
                <div className="profile-info" style={{ color: '#555' }}>
                    {(userInfoState || userInfo).email ? (userInfoState || userInfo).email : <span style={{ color: '#aaa', fontStyle: 'italic' }}>Email not set</span>}
                </div>
                <div className="profile-info" style={{ color: '#555', marginBottom: '1rem' }}>
                    {(userInfoState || userInfo).biography ? (userInfoState || userInfo).biography : <span style={{ color: '#aaa', fontStyle: 'italic' }}>No biography</span>}
                </div>
                <div style={{ display: 'flex', justifyContent: 'center', gap: '2rem', marginBottom: '1rem' }}>
                    <button 
                        className="follow-count-button"
                        onClick={() => setShowFollowersFollowing(true)}
                    >
                        <b>Followers:</b> {(userInfoState || userInfo).followerCount || 0}
                    </button>
                    <button 
                        className="follow-count-button"
                        onClick={() => setShowFollowersFollowing(true)}
                    >
                        <b>Following:</b> {(userInfoState || userInfo).followingCount || 0}
                    </button>
                </div>
                {!isOwnProfile && currentUser && (
                    <div style={{ display: 'flex', justifyContent: 'center', marginTop: 0, marginBottom: '1rem' }}>
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
                    </div>
                )}
                {followError && (
                    <div className="error-message" style={{ marginTop: '0.5rem' }}>
                        {followError}
                    </div>
                )}
            </div>

            {postsError && (
                <div className="error-message">
                    Error loading posts: {postsError}
                </div>
            )}

            <h2 className="page-title" style={{ textAlign: 'left', marginBottom: '1.5rem' }}>Posts by {username}</h2>
            <PostList posts={posts || []} />

            {showFollowersFollowing && (
                <div className="modal-overlay" onClick={handleFollowersFollowingClose}>
                    <div
                        className="modal-content"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="followers-following-title"
                        tabIndex="-1"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div className="modal-header">
                            <h3 id="followers-following-title">Followers & Following</h3>
                            <button 
                                className="modal-close"
                                onClick={handleFollowersFollowingClose}
                                tabIndex="0"
                                aria-label="Close Followers and Following dialog"
                            >
                                ×
                            </button>
                        </div>
                        <FollowersFollowing 
                            username={username} 
                            onUserClick={handleFollowersFollowingClose}
                        />
                    </div>
                </div>
            )}
        </div>
    )
}

export default UserProfilePage