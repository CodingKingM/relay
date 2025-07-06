import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { httpClient } from '../../utils/httpClient'
import { useAuth } from '../../hooks/useAuth'

function FollowersFollowing({ username, onUserClick }) {
    const [activeTab, setActiveTab] = useState('followers')
    const [followers, setFollowers] = useState([])
    const [following, setFollowing] = useState([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState(null)
    const { user: currentUser } = useAuth()

    useEffect(() => {
        const loadData = async () => {
            setLoading(true)
            setError(null)
            
            try {
                // Load both followers and following data
                const [followersData, followingData] = await Promise.all([
                    httpClient.getFollowers(username),
                    httpClient.getFollowing(username)
                ])
                
                setFollowers(followersData)
                setFollowing(followingData)
            } catch (err) {
                console.error(`Failed to load data:`, err)
                setError(err.message)
            } finally {
                setLoading(false)
            }
        }

        loadData()
    }, [username])

    const handleFollow = async (targetUsername, isFollowing) => {
        try {
            if (isFollowing) {
                await httpClient.unfollowUser(targetUsername)
            } else {
                await httpClient.followUser(targetUsername)
            }

            // Update the appropriate list
            if (activeTab === 'followers') {
                setFollowers(prev => 
                    prev.map(user => 
                        user.username === targetUsername 
                            ? { ...user, isFollowing: !user.isFollowing }
                            : user
                    )
                )
            } else {
                setFollowing(prev => 
                    prev.map(user => 
                        user.username === targetUsername 
                            ? { ...user, isFollowing: !user.isFollowing }
                            : user
                    )
                )
            }
        } catch (err) {
            console.error('Failed to toggle follow:', err)
            alert(`Failed to ${isFollowing ? 'unfollow' : 'follow'} user: ${err.message}`)
        }
    }

    const currentList = activeTab === 'followers' ? followers : following

    return (
        <div className="followers-following-container">
            <div className="tabs">
                <button 
                    className={`tab ${activeTab === 'followers' ? 'active' : ''}`}
                    onClick={() => setActiveTab('followers')}
                >
                    Followers ({followers.length})
                </button>
                <button 
                    className={`tab ${activeTab === 'following' ? 'active' : ''}`}
                    onClick={() => setActiveTab('following')}
                >
                    Following ({following.length})
                </button>
            </div>

            {loading && (
                <div className="loading">Loading {activeTab}...</div>
            )}

            {error && (
                <div className="error-message">
                    Error loading {activeTab}: {error}
                </div>
            )}

            {!loading && !error && currentList.length === 0 && (
                <div className="empty-state">
                    No {activeTab} found
                </div>
            )}

            {!loading && !error && currentList.length > 0 && (
                <div className="user-list">
                    {currentList.map(user => (
                        <div key={user.username} className="user-item">
                            <Link 
                                to={`/user/${user.username}`} 
                                className="user-info"
                                onClick={onUserClick}
                            >
                                <div className="user-avatar">
                                    {user.username.charAt(0).toUpperCase()}
                                </div>
                                <div className="user-details">
                                    <div className="username">{user.username}</div>
                                </div>
                            </Link>
                            {(() => {
                                const shouldShowButton = currentUser?.username !== user.username;
                                console.log('FollowersFollowing follow button debug:', {
                                    currentUser: currentUser?.username,
                                    userUsername: user.username,
                                    shouldShowButton,
                                    isEqual: currentUser?.username === user.username
                                });
                                return shouldShowButton ? (
                                    <button
                                        className={`follow-button ${user.isFollowing ? 'following' : ''}`}
                                        onClick={() => handleFollow(user.username, user.isFollowing)}
                                    >
                                        {user.isFollowing ? 'Unfollow' : 'Follow'}
                                    </button>
                                ) : null;
                            })()}
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}

export default FollowersFollowing 