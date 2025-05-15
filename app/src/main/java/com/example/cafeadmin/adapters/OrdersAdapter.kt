import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cafeadmin.adapters.SingleOrderAdapter
import com.example.cafeadmin.databinding.ItemOrdersBinding
import com.example.cafeadmin.models.TableOrder
class OrdersAdapter(
    private var orders: List<TableOrder> = emptyList()
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(private val binding: ItemOrdersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: TableOrder) {
            with(binding) {
                // Kichik adapterni ulaymiz
                val itemAdapter = SingleOrderAdapter()
                rvItems.layoutManager = LinearLayoutManager(root.context)
                rvItems.adapter = itemAdapter

                // Order (ovqatlar) ro‘yxatini uzatamiz
                itemAdapter.submitList(order.items)

                tvPrice.text = "Umumiy summa: ${order.total} so'm"
                count.text = "Ovqat soni: ${order.items.size}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrdersBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<TableOrder>) {
        this.orders = newOrders
        notifyDataSetChanged()
    }
}
