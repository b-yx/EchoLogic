import { createRouter, createWebHashHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import RecordsView from '../views/RecordsView.vue'
import TagsView from '../views/TagsView.vue'
import CollectionsView from '../views/CollectionsView.vue'
import CollectionIncubatorView from '../views/CollectionIncubatorView.vue'
import ContentGenerateView from '../views/ContentGenerateView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/records',
    name: 'records',
    component: RecordsView
  },
  {
    path: '/tags',
    name: 'tags',
    component: TagsView
  },
  {
    path: '/collections',
    name: 'collections',
    component: CollectionsView
  },
  {
    path: '/collection/:id',
    name: 'collection-incubator',
    component: CollectionIncubatorView,
    props: true
  },
  {
    path: '/content-generate',
    name: 'content-generate',
    component: ContentGenerateView
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
