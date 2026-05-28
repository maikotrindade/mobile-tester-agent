import './App.css'
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from './pages/home/Home';
import Settings from './pages/settings/Settings';
import Reports from './pages/reports/Reports';
import About from './pages/about/About';
import TopNav from './TopNav';
import { Footer } from './Footer';
import { LanguageProvider } from './i18n/LanguageContext';

function App() {
  return (
    <LanguageProvider>
      <BrowserRouter>
        <TopNav />
        <main className="page">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/settings" element={<Settings />} />
            <Route path="/reports" element={<Reports />} />
            <Route path="/about" element={<About />} />
          </Routes>
        </main>
        <Footer />
      </BrowserRouter>
    </LanguageProvider>
  )
}

export default App
