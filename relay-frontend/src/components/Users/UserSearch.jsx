import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { httpClient } from '../../utils/httpClient'
import { useAuth } from '../../hooks/useAuth'

function UserSearch() {
    const [query, setQuery] = useState('')
    const [users, setUsers] = useState([])
    const [loading, setLoading] = useState(false)
    const [searchError, setSearchError] = useState(null)
    const { user: currentUser } = useAuth()

    useEffect(() => {
        const searchUsers = async () => {
            if (!query.trim()) {
                setUsers([])
                setSearchError(null)
                return
            }

            setLoading(true)
            setSearchError(null)

            try {

                const results = await httpClient.findUsers(query)
                setUsers(results || [])
            } catch (err) {
                console.error('Search failed:', err)
                setSearchError(err.message)
                setUsers([])
            } finally {
                setLoading(false)
            }
        }

        const timeoutId = setTimeout(searchUsers, 300)
        return () => clearTimeout(timeoutId)
    }, [query])

    const handleFollow = async (username, isFollowing) => {
        try {

            setUsers(prevUsers =>
                prevUsers.map(user =>
                    user.username === username
                        ? { ...user, isFollowing: !user.isFollowing }
                        : user
                )
            )


            if (isFollowing) {
                await httpClient.unfollowUser(username)
            } else {
                await httpClient.followUser(username)
            }

        } catch (err) {
            console.error('Failed to toggle follow:', err)


            setUsers(prevUsers =>
                prevUsers.map(user =>
                    user.username === username
                        ? { ...user, isFollowing: !user.isFollowing }
                        : user
                )
            )


            alert(`Failed to ${isFollowing ? 'unfollow' : 'follow'} user: ${err.message}`)
        }
    }

    return (
        <div className="search-container" style={{ position: 'relative' }}>
            <input
                type="text"
                className="search-input"
                placeholder="Search users by username..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                autoFocus
            />

            {loading && <div className="loading">Searching...</div>}

            {searchError && (
                <div className="error-message">
                    Error: {searchError}
                </div>
            )}

            {!loading && query && users.length === 0 && !searchError && (
                <div className="empty-state">No users found matching "{query}"</div>
            )}

            {!loading && users.length > 0 && (
                <div className="user-search-dropdown">
                    <div className="user-list">
                        {users.map(user => (
                            <div key={user.username} className="user-item">
                                <Link to={`/user/${user.username}`} className="user-info">
                                    {user.username}
                                </Link>
                                {(() => {
                                    const shouldShowButton = currentUser?.username !== user.username;
                                    console.log('Follow button debug:', {
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
                </div>
            )}
        </div>
    )
}

export default UserSearch