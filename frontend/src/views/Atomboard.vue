<script>
  import Atomboard from "@/components/Atomboard/App.tsx";

  export default {
    components: {
      'Atomboard': Atomboard
    },
    methods: {
      backToDetail() {
        this.$router.push({name: 'RankList'});
      }
    }
  }
</script>

<template>
  <div>
    <Atomboard></Atomboard>
    <img src="@/components/Atomboard/backIcon.svg" alt="backIcon Logo" v-on:click="backToDetail"
    style="
         position: absolute;
         top: 8px;
         left: 25px;
         width: 45px
    "/>

  </div>


</template>

<style scoped>

</style>