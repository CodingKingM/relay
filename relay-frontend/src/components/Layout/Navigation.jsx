import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

function Navigation() {
    const { isAuthenticated, user, logout } = useAuth()
    const location = useLocation()
    const navigate = useNavigate()

    const handleLogout = async () => {
        await logout()
        navigate('/login')
    }

    const isActive = (path) => location.pathname === path

    return (
        <nav className="navbar">
            <div className="nav-container">
                <Link to="/" className="nav-brand">Relay</Link>

                {isAuthenticated ? (
                    <ul className="nav-links">
                        <li>
                            <Link
                                to="/timeline"
                                className={`nav-link ${isActive('/timeline') ? 'active' : ''}`}
                            >
                                Timeline
                            </Link>
                        </li>
                        <li>
                            <Link
                                to="/my-posts"
                                className={`nav-link ${isActive('/my-posts') ? 'active' : ''}`}
                            >
                                My Posts
                            </Link>
                        </li>
                        <li>
                            <Link
                                to="/search"
                                className={`nav-link ${isActive('/search') ? 'active' : ''}`}
                            >
                                Search Users
                            </Link>
                        </li>
                        <li>
                            <Link
                                to="/profile"
                                className={`nav-link ${isActive('/profile') ? 'active' : ''}`}
                            >
                                My Profile
                            </Link>
                        </li>
                        <li>
                            <span className="nav-link">Hi, {user?.username}</span>
                        </li>
                        <li>
                            <button
                                onClick={handleLogout}
                                className="nav-link"
                                style={{ background: 'none', border: 'none', cursor: 'pointer' }}
                            >
                                Logout
                            </button>
                        </li>
                    </ul>
                ) : (
                    <ul className="nav-links">
                        <li>
                            <Link
                                to="/login"
                                className={`nav-link ${isActive('/login') ? 'active' : ''}`}
                            >
                                Login
                            </Link>
                        </li>
                        <li>
                            <Link
                                to="/register"
                                className={`nav-link ${isActive('/register') ? 'active' : ''}`}
                            >
                                Register
                            </Link>
                        </li>
                    </ul>
                )}
            </div>
        </nav>
    )
}

export default Navigation