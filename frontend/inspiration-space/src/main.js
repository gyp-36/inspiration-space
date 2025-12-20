import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import App from './App.vue'
import router from './router'
import 'element-plus/dist/index.css'
import Navigation from './components/home/Navigation.vue'
const app = createApp(App)

app.use(createPinia())
app.use(ElementPlus)
app.use(router)

app.component('Navigation', Navigation)
app.mount('#app')
