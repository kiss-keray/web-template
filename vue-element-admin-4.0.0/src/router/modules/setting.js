/** When your routing table is too long, you can split it into small modules**/

import Layout from '@/layout'

const settingRouter = {
  path: '/setting',
  component: Layout,
  redirect: '/table/complex-table',
  name: 'setting',
  meta: {
    title: '设置',
    icon: 'setting'
  },
  children: [
    {
      path: 'set-ksc',
      component: () => import('@/views/setting/kSchudle'),
      name: 'setKsc',
      meta: { title: 'K类定时任务' }
    },
    {
      path: 'set-conf',
      component: () => import('@/views/table/dragTable'),
      name: 'setConf',
      meta: { title: '动态配置' }
    }
  ]
}
export default settingRouter
