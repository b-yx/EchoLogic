import http from './http'

export default{
    getAllTags(){
        return http.get('/tags')
    },

    getTagById(id){
        return http.get(`/tags/${id}`)
    },

    createTag(data){
        return http.post('/tags',data)
    },

    updateTag(id,data){
        return http.put(`/tags/${id}`,data)
    },

    deleteTag(id){
        return http.delete(`/tags/${id}`)
    },

    associateWithRecord(recordId, tagId) {
        return http.post(`/records/${recordId}/tags/${tagId}`)
    },
}