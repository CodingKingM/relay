import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './hooks/useAuth'
import Navigation from './components/Layout/Navigation'
import HomePage from './pages/HomePage'
import TimelinePage from './pages/TimelinePage'
import UserProfilePage from './pages/UserProfilePage'
import SearchPage from './pages/SearchPage'
import LoginForm from './components/Auth/LoginForm'
import RegisterForm from './components/Auth/RegisterForm'
import MyProfilePage from './pages/MyProfilePage'

function ProtectedRoute({ children }) {
    const { isAuthenticated, loading } = useAuth()

    if (loading) {
        return <div className="loading">Loading...</div>
    }

    return isAuthenticated ? children : <Navigate to="/login" />
}

function AppContent() {
    const { isAuthenticated } = useAuth()

    return (
        <div className="app">
            <Navigation />
            <main className="main-content">
                <Routes>
                    <Route path="/" element={isAuthenticated ? <Navigate to="/timeline" /> : <Navigate to="/login" />} />
                    <Route path="/login" element={isAuthenticated ? <Navigate to="/timeline" /> : <LoginForm />} />
                    <Route path="/register" element={isAuthenticated ? <Navigate to="/timeline" /> : <RegisterForm />} />
                    <Route path="/timeline" element={<ProtectedRoute><TimelinePage /></ProtectedRoute>} />
                    <Route path="/my-posts" element={<ProtectedRoute><HomePage /></ProtectedRoute>} />
                    <Route path="/search" element={<ProtectedRoute><SearchPage /></ProtectedRoute>} />
                    <Route path="/user/:username" element={<ProtectedRoute><UserProfilePage /></ProtectedRoute>} />
                    <Route path="/profile" element={<ProtectedRoute><MyProfilePage /></ProtectedRoute>} />
                    <Route path="*" element={<Navigate to="/" />} />
                </Routes>
            </main>
        </div>
    )
}

function App() {
    return (
        <Router>
            <AuthProvider>
                <AppContent />
            </AuthProvider>
        </Router>
    )
}

export default App