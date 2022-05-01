import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { createHtmlPlugin } from 'vite-plugin-html'

// https://vitejs.dev/config/
export default defineConfig(({command, mode}) => {
    const mainJS = `/target/scala-2.13/slinky-${mode === 'production' ? 'opt' : 'fastopt'}/main.js`
    console.log('mainJS', mainJS)
    const script = `<script type="module" src="${mainJS}"></script>`

    return {
        plugins: [
            react(),
            createHtmlPlugin({
                minify: (process.env.NODE_ENV === 'production'),
                inject: {data: {script}}
            })],
        base: './',
        build: {
            outDir: '../../docs/slinky',
            emptyOutDir: true
        },
        server: {
            port: 8001,
        }
    }
})
