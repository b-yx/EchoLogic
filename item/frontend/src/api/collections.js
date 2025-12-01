import http from './http'

export default{
    getAllCollections(){
        return http.get('/collections')
    },

    getCollectionById(id){
        return http.get(`/collections/${id}`)
    },

    createCollection(data){
        return http.post('/collections',data)
    },

    updateCollection(id,data){
        return http.put(`/collections/${id}`,data)
    },

    deleteCollection(id){
        return http.delete(`/collections/${id}`)
    },
}