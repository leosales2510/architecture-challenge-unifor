import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// O proxy do Vite faz o papel de um "gateway" de desenvolvimento: o navegador fala
// só com localhost:5173 e cada prefixo é encaminhado ao microsserviço dono do recurso.
// Assim não é preciso liberar CORS nos serviços.
const servico = (porta) => ({ target: `http://localhost:${porta}`, changeOrigin: true })

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/catalogo': servico(8081),
      '/api/estoque': servico(8082),
      '/api/pagamentos': servico(8083),
      '/api/pedidos': servico(8084),
      '/api/cupons': servico(8084),
    },
  },
})
